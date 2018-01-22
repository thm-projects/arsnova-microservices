package de.thm.arsnova.roomservice

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.persistence.PersistentActor
import akka.util.Timeout
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.shared.Exceptions._
import de.thm.arsnova.shared.entities.export.{AnswerOptionExport, ContentExport, FreetextAnswerExport}
import de.thm.arsnova.shared.entities.{AnswerOption, ChoiceAnswerStatistics, Content, Room, User}
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.events.RoomEvents.{RoomCreated, RoomDeleted}
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands.{GetChoiceStatistics, ImportChoiceAnswers}
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands.{GetFreetextStatistics, ImportFreetextAnswers}
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.servicecommands.RoomCommands.GetRoom
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.shards.{AnswerListShard, EventShard, RoomShard, UserShard}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object ContentActor {
  def props(authRouter: ActorRef): Props =
    Props(new ContentActor(authRouter: ActorRef))
}

class ContentActor(authRouter: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val eventRegion = ClusterSharding(context.system).shardRegion(EventShard.shardName)

  val userRegion = ClusterSharding(context.system).shardRegion(UserShard.shardName)

  val roomRegion = ClusterSharding(context.system).shardRegion(RoomShard.shardName)

  val answerListActor = ClusterSharding(context.system).shardRegion(AnswerListShard.shardName)

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var state: Option[Content] = None

  override def persistenceId: String = self.path.parent.name + "-"  + self.path.name

  override def receiveRecover: Receive = {
    case ContentCreated(c) =>
      state = Some(c)
      context.become(contentCreated)
    case ContentDeleted(c) =>
      state = None
      context.become(initial)
  }

  override def receiveCommand: Receive = initial

  def handleEvents(sep: RoomEventPackage) = {
    sep.event match {
      case _ =>
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
    case cmd@CreateContent(id, c, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForRoom(userId, c.roomId)).mapTo[String] map { role =>
        ContentCommandWithRole(cmd, role, ret)
      } pipeTo self
    }) (sender)
    case Import(id, roomId, exportedContent) => ((ret: ActorRef) => {
      var content = Content(exportedContent, roomId)
      content = content.copy(id = Some(id))
      contentToType(content) match {
        case "choice" => {
          exportedContent.answerOptions match {
            case Some(options) => {
              var index: Int = 0
              val answerOptions: Seq[AnswerOption] = options.map { ao =>
                val a = AnswerOption(ao, index, id)
                index = index + 1
                a
              }
              content = content.copy(answerOptions = Some(answerOptions))
              answerListActor ! ImportChoiceAnswers(content.roomId, id, exportedContent.answerOptions.get)
            }
          }
        }
        case "freetext" => {
          exportedContent.answers match {
            case Some(answers) => {
              answerListActor ! ImportFreetextAnswers(roomId, id, answers)
            }
          }
        }
      }
      state = Some(content)
      context.become(contentCreated)
      persist(ContentCreated(content))(e => e)
    }) (sender)

    case ContentCommandWithRole(cmd, role, ret) => {
      cmd match {
        case CreateContent(id, c, userId) => {
          if (role == "owner") {
            state = Some(c)
            ret ! Success(c)
            val e = ContentCreated(c)
            eventRegion ! RoomEventPackage(c.roomId, e)
            context.become(contentCreated)
            persist(e)(e => e)
          } else {
            ret ! Failure(InsufficientRights(role, "Create Content"))
          }
        }
      }
    }

    case _ => {
      sender() ! Failure(ResourceNotFound("content"))
    }
  }

  def contentCreated: Receive = {
    case GetContent(id) => {
      sender() ! Success(state.get)
    }
    case cmd@DeleteContent(id, userId) => ((ret: ActorRef) => {
      val c = state.get
      (userRegion ? GetRoleForRoom(userId, c.roomId)).mapTo[String] map { role =>
        ContentCommandWithRole(cmd, role, ret)
      } pipeTo self
    }) (sender)
    case GetExport(id) => ((ret: ActorRef) => {
      val c = state.get
      var export = ContentExport(c)
      contentToType(c) match {
        case "choice" => {
          (answerListActor ? GetChoiceStatistics(c.roomId, c.id.get)).mapTo[ChoiceAnswerStatistics].map { s =>
            val answerOptionExportList = c.answerOptions.map { seq =>
              seq map { option =>
                AnswerOptionExport(option, s.choices(option.index))
              }
            }
            export = export.copy(answerOptions = answerOptionExportList, abstentionCount = s.abstentions)
            ret ! Success(export)
          }
        }
        case "freetext" => {
          (answerListActor ? GetFreetextStatistics(c.roomId, c.id.get)).mapTo[Seq[FreetextAnswerExport]].map { seq =>
            export = export.copy(answers = Some(seq))
            ret ! Success(export)
          }
        }
      }
    }) (sender)

    case cmd@SetRound(contentId, userId, round) => ((ret: ActorRef) => {
      val c = state.get
      (userRegion ? GetRoleForRoom(userId, c.roomId)).mapTo[String] map { role =>
        ContentCommandWithRole(cmd, role, ret)
      } pipeTo self
    }) (sender)

    case ContentCommandWithRole(cmd, role, ret) => {
      cmd match {
        case DeleteContent(id, userId) => {
          val c = state.get
          if (role == "owner") {
            state = None
            ret ! Success(c)
            eventRegion ! RoomEventPackage(c.roomId, ContentDeleted(c))
            context.become(initial)
            persist(ContentDeleted(c))(e => e)
          } else {
            ret ! Failure(InsufficientRights(role, "Delete Content"))
          }
        }
        case SetRound(contentId, userId, round) => {
          val c = state.get
          if (role == "owner") {
            val updated = c.copy(votingRound = round)
            state = Some(updated)
            persist(ContentUpdated(updated))(e => e)
            ret ! Success(round)
          } else {
            ret ! Failure(InsufficientRights(role, "Delete Content"))
          }
        }
      }
    }

    case sep: RoomEventPackage => handleEvents(sep)
  }
}
