package de.thm.arsnova.commentservice

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import akka.actor.ActorRef
import akka.actor.Props
import akka.util.Timeout
import akka.pattern.ask
import akka.persistence.PersistentActor
import de.thm.arsnova.shared.Exceptions.{NoSuchSession, ResourceNotFound}
import de.thm.arsnova.shared.entities.{Comment, Session, User}
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.events.CommentEvents._
import de.thm.arsnova.shared.events.SessionEvents.{SessionCreated, SessionDeleted}
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString
import de.thm.arsnova.shared.servicecommands.CommentCommands._
import de.thm.arsnova.shared.servicecommands.SessionCommands.GetSession

object CommentListActor {
  def props(eventRegion: ActorRef, authRouter: ActorRef, sessionRegion: ActorRef): Props =
    Props(new CommentListActor(eventRegion: ActorRef, authRouter: ActorRef, sessionRegion: ActorRef))
}

class CommentListActor(eventRegion: ActorRef, authRouter: ActorRef, sessionRegion: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private val commentlist: collection.mutable.HashMap[UUID, Comment] =
    collection.mutable.HashMap.empty[UUID, Comment]

  def tokenToUser(tokenstring: String): Future[Try[User]] = {
    (authRouter ? GetUserFromTokenString(tokenstring)).mapTo[Try[User]]
  }

  override def persistenceId: String = self.path.parent.name + "-"  + self.path.name

  override def receiveRecover: Receive = {
    case SessionCreated(session) => {
      context.become(sessionCreated)
    }
    case SessionDeleted(session) => {
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

  def handleEvents(sep: SessionEventPackage) = {
    sep.event match {
      case SessionCreated(session) => {
        context.become(sessionCreated)
        persist(SessionCreated(session))(e => e)
      }
      case SessionDeleted(session) => {
        commentlist.clear()
        context.become(initial)
        persist(SessionDeleted(session))(e => e)
      }
    }
  }

  def initial: Receive = {
    case sep: SessionEventPackage => handleEvents(sep)
    case cmd: CommentCommand => {
      // query session service just in case the session creation event got lost
      (sessionRegion ? GetSession(cmd.sessionId)).mapTo[Try[Session]] map {
        case Success(session) => {
          context.become(sessionCreated)
          context.self ! cmd
          persist(SessionCreated(session))(e => e)
        }
        case Failure(t) => sender() ! Failure(NoSuchSession(Left(cmd.sessionId)))
      }
    }
  }

  def sessionCreated: Receive = {
    case CreateComment(sessionId, comment, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Success(user) => {
          commentlist += comment.id.get -> comment
          ret ! Success(comment)
          val e = CommentCreated(comment)
          eventRegion ! SessionEventPackage(comment.sessionId, e)
          persist(e) { e => e }
        }
        case Failure(t) => ret ! t
      }
    }) (sender)
    case GetComment(sessionId, id) => ((ret: ActorRef) => {
      commentlist.get(id) match {
        case Some(c) => ret ! Some(c)
        case None => ret ! Failure(ResourceNotFound(s"comment with id: $id"))
      }
    }) (sender)
    case GetUnreadComments(sessionId) => ((ret: ActorRef) => {
      val unreads: Seq[Comment] = commentlist.values.map(identity).toSeq.filter(_.isRead == false)
      ret ! Success(unreads)
      unreads foreach { c =>
        commentlist += c.id.get -> c
        val e = CommentUpdated(c)
        eventRegion ! SessionEventPackage(c.sessionId, e)
        persist(e) { e => e }
      }
    }) (sender)
    case DeleteComment(sessionId, id) => ((ret: ActorRef) => {
      commentlist.remove(id) match {
        case Some(c) => {
          val e = CommentDeleted(c)
          eventRegion ! SessionEventPackage(c.sessionId, e)
          persist(e) { e => e }
        }
      }
    }) (sender)

    case sep: SessionEventPackage => handleEvents(sep)
  }
}
