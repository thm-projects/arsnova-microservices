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
import de.thm.arsnova.shared.Exceptions.NoSuchSession
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
    case CommentCreated(comment) => {
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
      case SessionDeleted(id) => {
        CommentRepository.deleteAllSessionContent(id)
        commentlist.clear()
        context.become(initial)
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
    case GetComment(sessionId, id) => ((ret: ActorRef) => {
      commentlist.get(id) match {
        case Some(c) => ret ! Some(c)
        case None =>
          CommentRepository.findById(id) map {
            case Some(c) => {
              commentlist += id -> c
              ret ! Some(c)
            }
            case None => ret ! None
          }
      }
    }) (sender)
    case DeleteComment(sessionId, id) => ((ret: ActorRef) => {
      commentlist.remove(id)
      CommentRepository.delete(id)
    }) (sender)

    case sep: SessionEventPackage => handleEvents(sep)
  }
}
