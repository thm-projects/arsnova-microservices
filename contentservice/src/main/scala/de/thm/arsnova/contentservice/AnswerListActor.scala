package de.thm.arsnova.contentservice

import java.util.UUID

import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.ReceiveTimeout
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.Passivate
import akka.persistence.PersistentActor
import akka.routing.RoundRobinPool
import de.thm.arsnova.contentservice.repositories.{ChoiceAnswerRepository, FreetextAnswerRepository}
import de.thm.arsnova.shared.entities.{ChoiceAnswer, Content, FreetextAnswer, Session, User}
import de.thm.arsnova.shared.events.SessionEvents.{SessionCreated, SessionDeleted, SessionEvent, SessionUpdated}
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands._
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands._
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.Exceptions
import de.thm.arsnova.shared.Exceptions.{InsufficientRights, NoSuchSession, NoUserException, ResourceNotFound}
import de.thm.arsnova.shared.events.ChoiceAnswerEvents.ChoiceAnswerCreated
import de.thm.arsnova.shared.events.FreetextAnswerEvents._
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.events.SessionEventPackage

object AnswerListActor {
  def props(eventRegion: ActorRef, authRouter: ActorRef, contentRegion: ActorRef): Props =
    Props(new AnswerListActor(eventRegion: ActorRef, authRouter: ActorRef, contentRegion: ActorRef))
}

class AnswerListActor(eventRegion: ActorRef, authRouter: ActorRef, contentRegion: ActorRef) extends PersistentActor {

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

  private var workRouter: Option[ActorRef] = None

  override def receiveRecover: Receive = {
    case ContentCreated(content) => {
      contentToType(content) match {
        case "choice" => {
          workRouter = Some(context.actorOf(RoundRobinPool(10)
            .props(ChoiceAnswerActor.props(authRouter))))
          context.become(choiceContentCreated)
        }
        case "freetext" => {
          workRouter = Some(context.actorOf(RoundRobinPool(10)
            .props(FreetextAnswerActor.props(authRouter))))
          context.become(freetextContentCreated)
        }
      }
    }
    case ContentDeleted(content) => {
      context.become(initial)
      choiceAnswerList.clear()
      freetextAnswerList.clear()
      workRouter = None
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
        contentToType(content) match {
          case "choice" => {
            ChoiceAnswerRepository.deleteAllByContentId(content.id.get)
          }
          case "freetext" => {
            FreetextAnswerRepository.deleteAllByContentId(content.id.get)
          }
        }
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
          persist(ContentCreated(c))
        }
        case Failure(t) => sender() ! Failure(ResourceNotFound("question"))
      }
    }
  }

  def choiceContentCreated: Receive = {
    case sep: SessionEventPackage => handleEvents(sep)
    case c: CreateChoiceAnswer => ((ret: ActorRef) => {
      (workRouter.get ? c).mapTo[Try[ChoiceAnswer]] map { res =>
        ret ! res
        res match {
          case Success(a) => {
            eventRegion ! SessionEventPackage(a.sessionId, ChoiceAnswerCreated(a))
            choiceAnswerList += a.id.get -> a
          }
        }
      }
    }) (sender)
  }

  def freetextContentCreated: Receive = {
    case sep: SessionEventPackage => handleEvents(sep)
    case c: CreateFreetextAnswer => ((ret: ActorRef) => {
      (workRouter.get ? c).mapTo[Try[FreetextAnswer]] map { res =>
        ret ! res
        res match {
          case Success(a) => {
            eventRegion ! SessionEventPackage(a.sessionId, FreetextAnswerCreated(a))
            freetextAnswerList += a.id.get -> a
          }
        }
      }
    }) (sender)
  }
}
