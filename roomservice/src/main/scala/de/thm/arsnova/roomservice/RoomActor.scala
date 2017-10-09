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
import de.thm.arsnova.shared.entities.{Room, User}
import de.thm.arsnova.shared.events.RoomEvents.{RoomCreated, RoomDeleted, RoomEvent, RoomUpdated}
import de.thm.arsnova.shared.servicecommands.RoomCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.Exceptions
import de.thm.arsnova.shared.Exceptions.{InsufficientRights, NoSuchRoom, NoUserException}
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.shards.{EventShard, UserShard}

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

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var state: Option[Room] = None

  override def receiveRecover: Receive = {
    case RoomCreated(room) => {
      state = Some(room)
      context.become(roomCreated)
    }
    case RoomUpdated(room) => {
      state = Some(room)
    }
    case RoomDeleted(room) => {
      state = None
      context.become(initial)
    }
    case s: Any => println(s)
  }

  override def receiveCommand: Receive = initial

  def initial: Receive = {
    case GetRoom(id) => ((ret: ActorRef) => {
      ret ! Failure(NoSuchRoom(Left(id)))
    }) (sender)
    case CreateRoom(id, room, userId) => ((ret: ActorRef) => {
      state = Some(room)
      context.become(roomCreated)
      ret ! Success(room)
      eventRegion ! RoomEventPackage(id, RoomCreated(room))
      persist(RoomCreated(room))(e => println(e))
    }) (sender)
    case DeleteRoom(id, userId) => {
      sender() ! NoSuchRoom(Left(id))
    }
  }

  def roomCreated: Receive = {
    case GetRoom(id) => ((ret: ActorRef) => {
      state match {
        case Some(room) => ret ! Success(room)
        case None => ret ! Failure(NoSuchRoom(Left(id)))
      }
    }) (sender)
    case UpdateRoom(id, room, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForRoom(userId, id)).mapTo[String] map { role =>
        if (role == "owner") {
          state = Some(room)
          ret ! Success(room)
          eventRegion ! RoomEventPackage(id, RoomUpdated(room))
          persist(RoomUpdated(room))(e => println(e))
        } else {
          ret ! Failure(InsufficientRights(role, "Update Room"))
        }
      }
    }) (sender)
    case DeleteRoom(id, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForRoom(userId, id)).mapTo[String] map { role =>
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
    }) (sender)
  }
}