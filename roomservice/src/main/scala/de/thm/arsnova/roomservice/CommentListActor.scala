package de.thm.arsnova.roomservice

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.persistence.PersistentActor
import akka.util.Timeout
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.shared.Exceptions.{NoSuchRoom, ResourceNotFound}
import de.thm.arsnova.shared.entities.{Comment, Room, User}
import de.thm.arsnova.shared.events.CommentEvents._
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.events.RoomEvents.{RoomCreated, RoomDeleted}
import de.thm.arsnova.shared.servicecommands.CommentCommands._
import de.thm.arsnova.shared.servicecommands.RoomCommands.GetRoom
import de.thm.arsnova.shared.shards.{EventShard, RoomShard, UserShard}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object CommentListActor {
  def props(authRouter: ActorRef): Props =
    Props(new CommentListActor(authRouter: ActorRef))
}

class CommentListActor(authRouter: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val eventRegion = ClusterSharding(context.system).shardRegion(EventShard.shardName)

  val userRegion = ClusterSharding(context.system).shardRegion(UserShard.shardName)

  val roomRegion = ClusterSharding(context.system).shardRegion(RoomShard.shardName)

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private val commentlist: collection.mutable.HashMap[UUID, Comment] =
    collection.mutable.HashMap.empty[UUID, Comment]

  override def persistenceId: String = self.path.parent.name + "-"  + self.path.name

  override def receiveRecover: Receive = {
    case RoomCreated(room) => {
      context.become(roomCreated)
    }
    case RoomDeleted(room) => {
      context.become(initial)
    }

    case CommentCreated(comment) => {
      commentlist += comment.id.get -> comment
    }
    case CommentUpdated(comment) => {
      commentlist += comment.id.get -> comment
    }
    case CommentDeleted(comment) => {
      commentlist.remove(comment.id.get)
    }
  }

  override def receiveCommand: Receive = initial

  def handleEvents(sep: RoomEventPackage) = {
    sep.event match {
      case RoomCreated(room) => {
        context.become(roomCreated)
        persist(RoomCreated(room))(e => e)
      }
      case RoomDeleted(room) => {
        commentlist.clear()
        context.become(initial)
        persist(RoomDeleted(room))(e => e)
      }
    }
  }

  def initial: Receive = {
    case sep: RoomEventPackage => handleEvents(sep)
    case cmd: CommentCommand => {
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
    case CreateComment(roomId, comment, userId) => ((ret: ActorRef) => {
      commentlist += comment.id.get -> comment
      ret ! Success(comment)
      val e = CommentCreated(comment)
      eventRegion ! RoomEventPackage(comment.roomId, e)
      persist(e) { e => e }
    }) (sender)
    case GetComment(roomId, id) => ((ret: ActorRef) => {
      commentlist.get(id) match {
        case Some(c) => ret ! Some(c)
        case None => ret ! Failure(ResourceNotFound(s"comment with id: $id"))
      }
    }) (sender)
    case GetCommentsByRoomId(roomId) => ((ret: ActorRef) => {
      ret ! commentlist.values.map(identity).toSeq
    }) (sender)
    case GetUnreadComments(roomId) => ((ret: ActorRef) => {
      val unreads: Seq[Comment] = commentlist.values.map(identity).toSeq.filter(_.isRead == false)
      ret ! Success(unreads)
      unreads foreach { c =>
        commentlist += c.id.get -> c
        val e = CommentUpdated(c)
        eventRegion ! RoomEventPackage(c.roomId, e)
        persist(e) { e => e }
      }
    }) (sender)
    case DeleteComment(roomId, id) => ((ret: ActorRef) => {
      commentlist.remove(id) match {
        case Some(c) => {
          val e = CommentDeleted(c)
          eventRegion ! RoomEventPackage(c.roomId, e)
          persist(e) { e => e }
        }
      }
    }) (sender)

    case sep: RoomEventPackage => handleEvents(sep)
  }
}
