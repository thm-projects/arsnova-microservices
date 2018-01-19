package de.thm.arsnova.roomservice

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.persistence.PersistentActor
import akka.util.Timeout
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.shared.Exceptions.{InsufficientRights, ResourceNotFound}
import de.thm.arsnova.shared.entities._
import de.thm.arsnova.shared.entities.export.FreetextAnswerExport
import de.thm.arsnova.shared.events.ChoiceAnswerEvents._
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.events.FreetextAnswerEvents._
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.global.GuestUser
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands._
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands.GetRoleForRoom
import de.thm.arsnova.shared.shards.{ContentShard, EventShard, UserShard}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object AnswerListActor {
  def props(authRouter: ActorRef): Props =
    Props(new AnswerListActor(authRouter: ActorRef))
}

class AnswerListActor(authRouter: ActorRef) extends PersistentActor {

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val eventRegion = ClusterSharding(context.system).shardRegion(EventShard.shardName)

  val userRegion = ClusterSharding(context.system).shardRegion(UserShard.shardName)

  val contentRegion = ClusterSharding(context.system).shardRegion(ContentShard.shardName)

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  var answerOptions: Option[Seq[AnswerOption]] = None

  var votingRound: Int = 0

  private val choiceAnswerList: collection.mutable.HashMap[UUID, ChoiceAnswer] =
    collection.mutable.HashMap.empty[UUID, ChoiceAnswer]
  private val freetextAnswerList: collection.mutable.HashMap[UUID, FreetextAnswer] =
    collection.mutable.HashMap.empty[UUID, FreetextAnswer]

  override def receiveRecover: Receive = {
    case ContentCreated(content) => {
      contentToType(content) match {
        case "choice" => {
          context.become(choiceContentCreated)
          answerOptions = content.answerOptions
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
      answerOptions = None
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

  def handleEvents(sep: RoomEventPackage) = {
    sep.event match {
      case ContentCreated(content) => {
        contentToType(content) match {
          case "choice" => {
            context.become(choiceContentCreated)
            answerOptions = content.answerOptions
          }
          case "freetext" => {
            context.become(freetextContentCreated)
          }
        }
        persist(ContentCreated(content))(e => e)
      }
      case ContentDeleted(content) => {
        choiceAnswerList.clear()
        freetextAnswerList.clear()
        answerOptions = None
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
    case sep: RoomEventPackage => handleEvents(sep)
    case ImportChoiceAnswers(roomId, contentId, exportedAnswerOptions) => {
      var index: Int = 0
      answerOptions = Some(exportedAnswerOptions.map { ao =>
        val a = AnswerOption(ao, index, contentId)
        index = index + 1
        a
      })
      context.become(choiceContentCreated)
    }
    case ImportFreetextAnswers(roomId, contentId, exportedAnswers) => {
      exportedAnswers.map { eAnswer =>
        val newId = UUID.randomUUID()
        val guestUser = GuestUser()
        val answer = FreetextAnswer(Some(newId), guestUser.id, Some(contentId), Some(roomId), eAnswer.subject, eAnswer.text)
        freetextAnswerList += newId -> answer
        persistAsync(FreetextAnswerCreated(answer)) { e => e }
      }
      context.become(freetextContentCreated)
    }
  }

  def choiceContentCreated: Receive = {
    case sep: RoomEventPackage => handleEvents(sep)
    case GetChoiceAnswers(roomId, questionId) => {
      sender() ! choiceAnswerList.values.map(identity).toSeq
    }
    case GetChoiceAnswer(roomId, questionId, id) => {
      sender() ! choiceAnswerList.get(id)
    }
    case CreateChoiceAnswer(roomId, contentId, answer, userId) => ((ret: ActorRef) => {
      val awu = answer.copy(userId = Some(userId), roomId = Some(roomId), contentId = Some(contentId), round = Some(votingRound))
      ret ! Success(awu)
      eventRegion ! RoomEventPackage(roomId, ChoiceAnswerCreated(awu))
      choiceAnswerList += awu.id.get -> awu
      persist(ChoiceAnswerCreated(awu)) { e => e }
    }) (sender)
    case cmd@DeleteChoiceAnswer(roomId, questionId, id, userId) => ((ret: ActorRef) => {
      choiceAnswerList.get(id) match {
        case Some(a) => {
          if (a.userId == userId) {
            choiceAnswerList -= id
            eventRegion ! RoomEventPackage(a.roomId.get, ChoiceAnswerDeleted(a))
            ret ! Success(a)
            persist(ChoiceAnswerDeleted(a)) { e => e }
          } else {
            (userRegion ? GetRoleForRoom(userId, roomId)).mapTo[String] map { role =>
              ChoiceAnswerCommandWithRole(cmd, role, ret)
            } pipeTo self
          }
        }
        case None => {
          ret ! ResourceNotFound(s"choice answer $id")
        }
      }
    }) (sender)
    case GetChoiceStatistics(roomId, questionId) => ((ret: ActorRef) => {
      val list = choiceAnswerList.values.map(identity).toSeq
      var abstentionCount = 0
      val count: Array[Int] = new Array[Int](answerOptions.get.size)
      list.map { a =>
        if (a.abstention) {
          abstentionCount += 1
        } else {
          a.answerIndexes.map { seq =>
            seq.map { i =>
              count(i) += 1
            }
          }
        }
      }
      ret ! ChoiceAnswerStatistics(count, abstentionCount)
    }) (sender)

    case ChoiceAnswerCommandWithRole(cmd, role, ret) => {
      cmd match {
        case DeleteChoiceAnswer(roomId, questionId, id, userId) => {
          choiceAnswerList.get(id) match {
            case Some(a) => {
              if (role == "owner") {
                choiceAnswerList -= id
                eventRegion ! RoomEventPackage(a.roomId.get, ChoiceAnswerDeleted(a))
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

  def freetextContentCreated: Receive = {
    case sep: RoomEventPackage => handleEvents(sep)
    case GetFreetextAnswers(roomId, questionId) => {
      sender() ! freetextAnswerList.values.map(identity).toSeq
    }
    case GetFreetextAnswer(roomId, questionId, id) => {
      sender() ! freetextAnswerList.get(id)
    }
    case CreateFreetextAnswer(roomId, contentId, answer, userId) => ((ret: ActorRef) => {
      val awu = answer.copy(userId = Some(userId), roomId = Some(roomId), contentId = Some(contentId))
      ret ! Success(awu)
      eventRegion ! RoomEventPackage(roomId, FreetextAnswerCreated(awu))
      freetextAnswerList += awu.id.get -> awu
      persist(FreetextAnswerCreated(awu)) { e => e }
    }) (sender)
    case cmd@DeleteFreetextAnswer(roomId, questionId, id, userId) => ((ret: ActorRef) => {
      freetextAnswerList.get(id) match {
        case Some(a) => {
          if (a.userId == userId) {
            freetextAnswerList -= id
            eventRegion ! RoomEventPackage(a.roomId.get, FreetextAnswerDeleted(a))
            ret ! Success(a)
            persist(FreetextAnswerDeleted(a)) { e => e }
          } else {
            (userRegion ? GetRoleForRoom(userId, roomId)).mapTo[String] map { role =>
              FreetextAnswerCommandWithRole(cmd, role, ret)
            } pipeTo self
          }
        }
        case None => {
          ret ! ResourceNotFound(s"choice answer $id")
        }
      }
    }) (sender)
    case GetFreetextStatistics(roomId, questionId) => ((ret: ActorRef) => {
      val list = freetextAnswerList.values.map(identity).toSeq
      ret ! list.map(FreetextAnswerExport(_))
    }) (sender)

    case FreetextAnswerCommandWithRole(cmd, role, ret) => {
      cmd match {
        case DeleteFreetextAnswer(roomId, questionId, id, userId) => {
          freetextAnswerList.get(id) match {
            case Some(a) => {
              if (role == "owner") {
                freetextAnswerList -= id
                eventRegion ! RoomEventPackage(a.roomId.get, FreetextAnswerDeleted(a))
                ret ! Success(a)
                persist(FreetextAnswerDeleted(a)) { e => e }
              } else {
                ret ! Failure(InsufficientRights(role, "DeleteChoiceAnswer"))
              }
            }
          }
        }
      }
    }
  }
}
