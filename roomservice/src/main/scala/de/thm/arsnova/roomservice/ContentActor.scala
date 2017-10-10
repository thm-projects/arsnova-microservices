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

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var content: Option[Content] = None

  override def persistenceId: String = self.path.parent.name + "-"  + self.path.name

  override def receiveRecover: Receive = {
    case ContentCreated(c) =>
      content = Some(c)
    case ContentDeleted(c) =>
      content = None
  }

  override def receiveCommand: Receive = initial

  def handleEvents(sep: RoomEventPackage) = {
    sep.event match {
      case _ =>
    }
  }

  def initial: Receive = {
    case sep: RoomEventPackage => handleEvents(sep)
    case CreateContent(id, c, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForRoom(userId, c.roomId)).mapTo[String] map { role =>
        if (role == "owner") {
          content = Some(c)
          ret ! Success(c)
          val e = ContentCreated(c)
          eventRegion ! RoomEventPackage(c.roomId, e)
          persist(e)(_)
        } else {
          ret ! Failure(InsufficientRights(role, "Create Content"))
        }
      }
    }) (sender)
  }

  def contentCreated: Receive = {
    case GetContent(id) => {
      sender() ! Success(content.get)
    }
    case DeleteContent(id, userId) => ((ret: ActorRef) => {
      val c = content.get
      (userRegion ? GetRoleForRoom(userId, c.roomId)).mapTo[String] map { role =>
        if (role == "owner") {
          content = None
          ret ! Success(c)
          eventRegion ! RoomEventPackage(c.roomId, ContentDeleted(c))
          persist(ContentDeleted(c))(e => e)
        } else {
          ret ! Failure(InsufficientRights(role, "Delete Content"))
        }
      }
    }) (sender)

    case sep: RoomEventPackage => handleEvents(sep)
  }
}
