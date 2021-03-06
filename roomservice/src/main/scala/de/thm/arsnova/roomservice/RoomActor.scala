package de.thm.arsnova.roomservice

import java.util.UUID

import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.ReceiveTimeout
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ClusterSharding
import akka.cluster.sharding.ShardRegion.Passivate
import akka.persistence.PersistentActor
import de.thm.arsnova.shared.entities.{Comment, Content, ContentGroup, Room, User}
import de.thm.arsnova.shared.events.RoomEvents._
import de.thm.arsnova.shared.servicecommands.RoomCommands._
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.Exceptions
import de.thm.arsnova.shared.Exceptions.{InsufficientRights, NoSuchRoom, NoUserException, ResourceNotFound}
import de.thm.arsnova.shared.entities.export.{ContentExport, RoomExport}
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.servicecommands.CommentCommands.GetCommentsByRoomId
import de.thm.arsnova.shared.servicecommands.ContentGroupCommands._
import de.thm.arsnova.shared.shards._

import scala.concurrent.{ExecutionContext, Future}

object RoomActor {
  def props(authRouter: ActorRef): Props =
    Props(new RoomActor(authRouter: ActorRef))
}

class RoomActor(authRouter: ActorRef) extends PersistentActor {

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val eventRegion = ClusterSharding(context.system).shardRegion(EventShard.shardName)

  val userRegion = ClusterSharding(context.system).shardRegion(UserShard.shardName)

  val contentRegion = ClusterSharding(context.system).shardRegion(ContentShard.shardName)

  val answerListRegion = ClusterSharding(context.system).shardRegion(AnswerListShard.shardName)

  val commentListRegion = ClusterSharding(context.system).shardRegion(CommentShard.shardName)

  val contentGroupActor = context.actorOf(ContentGroupActor.props(contentRegion))

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var state: Option[Room] = None

  override def receiveRecover: Receive = {
    case RoomCreated(room) => {
      state = Some(room)
      context.become(roomCreated)
      contentGroupActor ! SetGroups(room.contentGroups)
    }
    case RoomUpdated(room) => {
      state = Some(room)
      contentGroupActor ! SetGroups(room.contentGroups)
    }
    case RoomDeleted(room) => {
      state = None
      context.become(initial)
    }
    case s: Any => println(s)
  }

  override def receiveCommand: Receive = initial

  def handleEvents(sep: RoomEventPackage) = {
    sep.event match {
      case ContentCreated(content) => {
        (contentGroupActor ? AddToGroup(content.group, content))
          .mapTo[collection.mutable.Map[String, ContentGroup]] map {cg =>
          UpdateContentGroups(cg.toMap)
        } pipeTo self
      }
      case ContentDeleted(content) => {
        (contentGroupActor ? RemoveFromGroup(content.group, content))
          .mapTo[collection.mutable.Map[String, ContentGroup]] map {cg =>
          UpdateContentGroups(cg.toMap)
        } pipeTo self
      }
    }
  }

  def contentToType(content: Content): String = {
    content.format match {
      case "mc" => "choice"
      case "freetext" => "freetext"
    }
  }

  def getContentFromIds(ids: Seq[UUID], ret: ActorRef) = {
    val contentListFutures: Seq[Future[Option[Content]]] = ids map { id =>
      (contentRegion ? GetContent(id)).mapTo[Try[Content]].map {
        case Success(content) => Some(content)
        case Failure(t) => None
      }
    }
    Future.sequence(contentListFutures).map { list =>
      ret ! list.flatten
    }
  }

  def initial: Receive = {
    case sep: RoomEventPackage => handleEvents(sep)
    case CreateRoom(id, room, userId) => ((ret: ActorRef) => {
      state = Some(room)
      context.become(roomCreated)
      ret ! Success(room)
      eventRegion ! RoomEventPackage(id, RoomCreated(room))
      persist(RoomCreated(room))(e => e)
    }) (sender)
    case ImportRoom(id, keyword, userId, exportedRoom) => ((ret: ActorRef) => {
      var room = Room(exportedRoom).copy(id = Some(id), keyword = Some(keyword), userId = Some(userId))
      var contentGroups = exportedRoom.contentGroups
      exportedRoom.content map { contentExport =>
        val newContentId = UUID.randomUUID()
        val oldContentId = contentExport.id
        // save content
        contentRegion ! Import(newContentId, id, contentExport)
        // replace old id with new id
        contentGroups = contentGroups map {
          case (k, v) => {
            val zipped = v.contentIds.zipWithIndex
            val toReplace = zipped.find(_._1 == oldContentId)
            val newIds = v.contentIds.updated(toReplace.get._2, newContentId)
            k -> ContentGroup(v.autoSort, newIds)
          }
        }
      }
      // tell content group actor about groups
      contentGroupActor ! SetGroups(contentGroups)
      room = room.copy(contentGroups = contentGroups)
      state = Some(room)
      context.become(roomCreated)
      persist(RoomCreated(room))(e => e)
      ret ! Success(room)
    }) (sender)

    case _ => {
      sender() ! Failure(ResourceNotFound("session"))
    }
  }

  def roomCreated: Receive = {
    case GetRoom(id) => ((ret: ActorRef) => {
      state match {
        case Some(room) => ret ! Success(room)
        case None => ret ! Failure(NoSuchRoom(Left(id)))
      }
    }) (sender)
    case c@ExportRoom(id, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForRoom(userId, id)).mapTo[String] map { role =>
        RoomCommandWithRole(c, role, ret)
      } pipeTo self
    }) (sender)
    case c@UpdateRoom(id, room, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForRoom(userId, id)).mapTo[String] map { role =>
        RoomCommandWithRole(c, role, ret)
      } pipeTo self
    }) (sender)
    case c@DeleteRoom(id, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForRoom(userId, id)).mapTo[String] map { role =>
        RoomCommandWithRole(c, role, ret)
      } pipeTo self
    }) (sender)

    case GetContentListByRoomId(roomId, group) => ((ret: ActorRef) => {
      contentGroupActor ! SendContent(ret, group)
    }) (sender)

    case UpdateContentGroups(groups) => {
      state = Some(state.get.copy(contentGroups = groups))
      persist(RoomUpdated(state.get))(e => e)
    }

    case RoomCommandWithRole(cmd, role, ret) => {
      cmd match {
        case UpdateRoom(id, room, userId) => {
          if (role == "owner") {
            state = Some(room)
            ret ! Success(room)
            eventRegion ! RoomEventPackage(id, RoomUpdated(room))
            persist(RoomUpdated(room))(e => println("yay"))
          } else {
            ret ! Failure(InsufficientRights(role, "Update Room"))
          }
        }
        case DeleteRoom(id, userId) => {
          if (role == "owner") {
            ret ! Success(state.get)
            val e = RoomDeleted(state.get)
            state = None
            context.become(initial)
            eventRegion ! RoomEventPackage(id, e)
            persist(e)(e => e)
          } else {
            ret ! Failure(InsufficientRights(role, "Delete Room"))
          }
        }
        case ExportRoom(id, userId) => {
          if (role == "owner") {
            val exported = RoomExport(state.get)
            val contentListFuture = (contentGroupActor ? GetExportList()).mapTo[Seq[ContentExport]]
            val commentListFuture = (commentListRegion ? GetCommentsByRoomId(id)).mapTo[Try[Seq[Comment]]]
            val ff: Future[(Seq[ContentExport], Try[Seq[Comment]])] = for {
              contentList <- contentListFuture
              commentList <- commentListFuture
            } yield (contentList, commentList)
            ff.map { t =>
              val cc = t._1
              val ccc = t._2.getOrElse(Nil)
              val fullExport = exported.copy(content = cc, comments = ccc)
              ret ! Success(fullExport)
            }
          } else {
            ret ! Failure(InsufficientRights(role, "Export Room"))
          }
        }
      }
    }

    case sep: RoomEventPackage => handleEvents(sep)
  }
}
