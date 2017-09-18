package de.thm.arsnova.roomservice

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.persistence.PersistentActor
import akka.util.Timeout
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.shared.Exceptions._
import de.thm.arsnova.shared.entities.{Content, Room, User}
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.events.RoomEvents.{RoomCreated, RoomDeleted}
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.servicecommands.RoomCommands.GetRoom
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.shards.{EventShard, RoomShard, UserShard}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object ContentListActor {
  def props(authRouter: ActorRef): Props =
    Props(new ContentListActor(authRouter: ActorRef))
}

class ContentListActor(authRouter: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val eventRegion = ClusterSharding(context.system).shardRegion(EventShard.shardName)

  val userRegion = ClusterSharding(context.system).shardRegion(UserShard.shardName)

  val roomRegion = ClusterSharding(context.system).shardRegion(RoomShard.shardName)

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private val contentlist: collection.mutable.HashMap[UUID, Content] =
    collection.mutable.HashMap.empty[UUID, Content]

  override def persistenceId: String = self.path.parent.name + "-"  + self.path.name

  override def receiveRecover: Receive = {
    case RoomCreated(room) => {
      context.become(roomCreated)
    }
    case RoomDeleted(id) => {
      context.become(initial)
    }
    case ContentCreated(c) =>
      contentlist += c.id.get -> c
  }

  override def receiveCommand: Receive = initial

  def handleEvents(sep: RoomEventPackage) = {
    sep.event match {
      case RoomCreated(room) => {
        context.become(roomCreated)
        persist(RoomCreated(room))(e => e)
      }
      case RoomDeleted(room) => {
        contentlist.clear()
        context.become(initial)
        persist(RoomDeleted(room))(e => e)
      }
    }
  }

  def initial: Receive = {
    case sep: RoomEventPackage => handleEvents(sep)
    case cmd: ContentCommand => {
      // query room service just in case the room creation event got lost
      (roomRegion ? GetRoom(cmd.roomId)).mapTo[Try[Room]] map {
        case Success(room) => {
          context.become(roomCreated)
          context.self ! cmd
          persist(RoomCreated(room))(e => e)
        }
        case Failure(t) => sender() ! Failure(NoSuchRoom(Left(cmd.roomId)))
      }
    }
  }

  def roomCreated: Receive = {
    case GetContent(roomId, id) => ((ret: ActorRef) => {
      contentlist.get(id) match {
        case Some(c) => ret ! Some(c)
        case None => ret ! None
      }
    }) (sender)
    case DeleteContent(roomId, id, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForRoom(userId, id)).mapTo[String] map { role =>
        if (role == "owner") {
          val c = contentlist.remove(id)
          ret ! Success(c.get)
          eventRegion ! RoomEventPackage(roomId, ContentDeleted(c.get))
          persist(ContentDeleted(c.get))(e => e)
        } else {
          ret ! Failure(InsufficientRights(role, "Delete Content"))
        }
      }
    }) (sender)
    case GetContentListByRoomId(roomId) => ((ret: ActorRef) => {
      // .map(identity) is needed due to serialization bug in scala
      // https://stackoverflow.com/questions/32900862/map-can-not-be-serializable-in-scala
      ret ! contentlist.values.map(identity).toSeq
    }) (sender)
    case GetContentListByRoomIdAndVariant(roomId, variant) => ((ret: ActorRef) => {
      ret ! contentlist.values.map(identity).toSeq.filter(_.variant == variant)
    }) (sender)
    case CreateContent(roomId, content, userID) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForRoom(userID, roomId)).mapTo[String] map { role =>
        if (role != "guest") {
          contentlist += content.id.get -> content
          ret ! Success(content)
          eventRegion ! RoomEventPackage(content.roomId, ContentCreated(content))
          persist(ContentCreated(content)) { e => e }
        } else {
          ret ! Failure(InsufficientRights(role, "CreateContent"))
        }
      }
    }) (sender)

    case sep: RoomEventPackage => handleEvents(sep)
  }
}
