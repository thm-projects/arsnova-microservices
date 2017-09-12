package de.thm.arsnova.sessionservice

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.persistence.PersistentActor
import akka.util.Timeout
import de.thm.arsnova.shared.Exceptions.{InsufficientRights, ResourceNotFound}
import de.thm.arsnova.shared.entities.{ChoiceAnswer, Content, FreetextAnswer, User}
import de.thm.arsnova.shared.events.ChoiceAnswerEvents._
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.events.FreetextAnswerEvents._
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands._
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands.GetRoleForSession

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object AnswerListActor {
  def props(eventRegion: ActorRef, authRouter: ActorRef, contentRegion: ActorRef, userRegion: ActorRef): Props =
    Props(new AnswerListActor(eventRegion: ActorRef, authRouter: ActorRef, contentRegion: ActorRef, userRegion: ActorRef))
}

class AnswerListActor(eventRegion: ActorRef, authRouter: ActorRef, contentRegion: ActorRef, userRegion: ActorRef) extends PersistentActor {

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  def tokenToUser(tokenstring: String): Future[Try[User]] = {
    (authRouter ? GetUserFromTokenString(tokenstring)).mapTo[Try[User]]
  }

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private val choiceAnswerList: collection.mutable.HashMap[UUID, ChoiceAnswer] =
    collection.mutable.HashMap.empty[UUID, ChoiceAnswer]
  private val freetextAnswerList: collection.mutable.HashMap[UUID, FreetextAnswer] =
    collection.mutable.HashMap.empty[UUID, FreetextAnswer]

  override def receiveRecover: Receive = {
    case ContentCreated(content) => {
      contentToType(content) match {
        case "choice" => {
          context.become(choiceContentCreated)
        }
        case "freetext" => {
          context.become(freetextContentCreated)
        }
      }
    }
    case ContentDeleted(content) => {
      context.become(initial)
      choiceAnswerList.clear()
      freetextAnswerList.clear()
    }

    case ChoiceAnswerCreated(answer) => {
      choiceAnswerList += answer.id.get -> answer
    }
    case ChoiceAnswerDeleted(answer) => {
      choiceAnswerList -= answer.id.get
    }

    case FreetextAnswerCreated(answer) => {
      freetextAnswerList += answer.id.get -> answer
    }
    case FreetextAnswerDeleted(answer) => {
      freetextAnswerList -= answer.id.get
    }
  }

  override def receiveCommand: Receive = initial

  def handleEvents(sep: SessionEventPackage) = {
    sep.event match {
      case ContentCreated(content) => {
        content.format match {
          case "mc" => context.become(choiceContentCreated)
          case "freetext" => context.become(freetextContentCreated)
        }
        persist(ContentCreated(content))(e => e)
      }
      case ContentDeleted(content) => {
        choiceAnswerList.clear()
        freetextAnswerList.clear()
        persist(ContentDeleted(content))(e => e)
      }
    }
  }

  def contentToType(content: Content): String = {
    content.format match {
      case "mc" => "choice"
      case "freetext" => "freetext"
    }
  }

  def initial: Receive = {
    case sep: SessionEventPackage => handleEvents(sep)
    case cmd: FreetextAnswerCommand => {
      // query question service just in case the content creation event got lost
      (contentRegion ? GetContent(cmd.sessionId, cmd.questionId))
        .mapTo[Try[Content]] map {
        case Success(c) => {
          contentToType(c) match {
            case "choice" => context.become(choiceContentCreated)
            case "freetext" => context.become(freetextContentCreated)
          }
          context.self ! cmd
          persist(ContentCreated(c)) { e => e }
        }
        case Failure(t) => sender() ! Failure(ResourceNotFound("question"))
      }
    }
  }

  def choiceContentCreated: Receive = {
    case sep: SessionEventPackage => handleEvents(sep)
    case GetChoiceAnswers(sessionId, questionId) => {
      sender() ! choiceAnswerList.values.map(identity).toSeq
    }
    case GetChoiceAnswer(sessionId, questionId, id) => {
      sender() ! choiceAnswerList.get(id)
    }
    case CreateChoiceAnswer(sessionId, questionId, answer, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Success(user) => {
          val awu = answer.copy(userId = user.id.get)
          ret ! Success(awu)
          eventRegion ! SessionEventPackage(awu.sessionId, ChoiceAnswerCreated(awu))
          choiceAnswerList += awu.id.get -> awu
          persist(ChoiceAnswerCreated(awu)) { e => e }
        }
      }
    }) (sender)
    case DeleteChoiceAnswer(sessionId, questionId, id, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Success(user) => {
          choiceAnswerList.get(id) match {
            case Some(a) => {
              if (a.userId == user.id.get) {
                choiceAnswerList -= id
                eventRegion ! SessionEventPackage(a.sessionId, ChoiceAnswerDeleted(a))
                ret ! Success(a)
                persist(ChoiceAnswerDeleted(a)) { e => e }
              } else {
                (userRegion ? GetRoleForSession(user.id.get, sessionId)).mapTo[String] map { role =>
                  if (role == "owner") {
                    choiceAnswerList -= id
                    eventRegion ! SessionEventPackage(a.sessionId, ChoiceAnswerDeleted(a))
                    ret ! Success(a)
                    persist(ChoiceAnswerDeleted(a)) { e => e }
                  } else {
                    ret ! Failure(InsufficientRights(role, "DeleteChoiceAnswer"))
                  }
                }
              }
            }
          }
        }
      }
    }) (sender)
  }

  def freetextContentCreated: Receive = {
    case sep: SessionEventPackage => handleEvents(sep)
    case GetFreetextAnswers(sessionId, questionId) => {
      sender() ! freetextAnswerList.values.map(identity).toSeq
    }
    case GetFreetextAnswer(sessionId, questionId, id) => {
      sender() ! freetextAnswerList.get(id)
    }
    case CreateFreetextAnswer(sessionId, questionId, answer, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Success(user) => {
          val awu = answer.copy(userId = user.id.get)
          ret ! Success(awu)
          eventRegion ! SessionEventPackage(awu.sessionId, FreetextAnswerCreated(awu))
          freetextAnswerList += awu.id.get -> awu
          persist(FreetextAnswerCreated(awu)) { e => e }
        }
      }
    }) (sender)
    case DeleteFreetextAnswer(sessionId, questionId, id, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Success(user) => {
          freetextAnswerList.get(id) match {
            case Some(a) => {
              if (a.userId == user.id.get) {
                freetextAnswerList -= id
                eventRegion ! SessionEventPackage(a.sessionId, FreetextAnswerDeleted(a))
                ret ! Success(a)
                persist(FreetextAnswerDeleted(a)) { e => e }
              } else {
                (userRegion ? GetRoleForSession(user.id.get, sessionId)).mapTo[String] map { role =>
                  if (role == "owner") {
                    freetextAnswerList -= id
                    eventRegion ! SessionEventPackage(a.sessionId, FreetextAnswerDeleted(a))
                    ret ! Success(a)
                    persist(FreetextAnswerDeleted(a)) { e => e }
                  } else {
                    ret ! Failure(InsufficientRights(role, "DeleteFreetextAnswer"))
                  }
                }
              }
            }
          }
        }
      }
    }) (sender)
  }
}
