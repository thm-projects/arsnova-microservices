package de.thm.arsnova.contentservice

import java.util.UUID

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
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
import de.thm.arsnova.contentservice.repositories.ContentRepository
import de.thm.arsnova.shared.entities.{Content, User, Session}
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.Exceptions._
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.events.SessionEvents.{SessionCreated, SessionDeleted}
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString
import de.thm.arsnova.shared.servicecommands.SessionCommands.GetSession

import scala.concurrent.{ExecutionContext, Future}

object ContentListActor {
  def props(eventRegion: ActorRef, authRouter: ActorRef, userRegion: ActorRef, sessionRegion: ActorRef): Props =
    Props(new ContentListActor(eventRegion: ActorRef, authRouter: ActorRef, userRegion: ActorRef, sessionRegion: ActorRef))
}

class ContentListActor(eventRegion: ActorRef, authRouter: ActorRef, userRegion: ActorRef, sessionRegion: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private val contentlist: collection.mutable.HashMap[UUID, Content] =
    collection.mutable.HashMap.empty[UUID, Content]

  def tokenToUser(tokenstring: String): Future[Try[User]] = {
    (authRouter ? GetUserFromTokenString(tokenstring)).mapTo[Try[User]]
  }

  override def persistenceId: String = self.path.parent.name + "-"  + self.path.name

  override def receiveRecover: Receive = {
    case SessionCreated(session) => {
      context.become(sessionCreated)
    }
    case SessionDeleted(id) => {
      context.become(initial)
    }
    case ContentCreated(c) =>
      contentlist += c.id.get -> c
  }

  override def receiveCommand: Receive = initial

  def handleEvents(sep: SessionEventPackage) = {
    sep.event match {
      case SessionCreated(session) => {
        context.become(sessionCreated)
        persist(SessionCreated(session))(e => e)
      }
      case SessionDeleted(session) => {
        ContentRepository.deleteAllSessionContent(session.id.get)
        contentlist.clear()
        context.become(initial)
        persist(SessionDeleted(session))(e => e)
      }
    }
  }

  def initial: Receive = {
    case sep: SessionEventPackage => handleEvents(sep)
    case cmd: ContentCommand => {
      // query session service just in case the session creation event got lost
      (sessionRegion ? GetSession(cmd.sessionid)).mapTo[Try[Session]] map {
        case Success(session) => {
          context.become(sessionCreated)
          context.self ! cmd
          persist(SessionCreated(session))(e => e)
        }
        case Failure(t) => sender() ! Failure(NoSuchSession(Left(cmd.sessionid)))
      }
    }
  }

  def sessionCreated: Receive = {
    case GetContent(sessionid, id) => ((ret: ActorRef) => {
      contentlist.get(id) match {
        case Some(c) => ret ! Some(c)
        case None =>
          ContentRepository.findById(id) map {
            case Some(c) => {
              contentlist += id -> c
              ret ! Some(c)
            }
            case None => ret ! None
          }
      }
    }) (sender)
    case GetContentListBySessionId(sessionid) => ((ret: ActorRef) => {
      // .map(identity) is needed due to serialization bug in scala
      // https://stackoverflow.com/questions/32900862/map-can-not-be-serializable-in-scala
      ret ! contentlist.values.map(identity).toSeq
    }) (sender)
    case GetContentListBySessionIdAndVariant(sessionid, variant) => ((ret: ActorRef) => {
      ret ! contentlist.values.map(identity).toSeq.filter(_.variant == variant)
    }) (sender)
    case CreateContent(sessionid, content, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Success(user) => {
          (userRegion ? GetRoleForSession(user.id.get, sessionid)).mapTo[String] map { role =>
            if (role != "guest") {
              ContentRepository.create(content) map { c =>
                contentlist += c.id.get -> c
                ret ! Success(c)
                persist(ContentCreated(c)) { e => e }
              }
            } else {
              ret ! Failure(InsufficientRights(role, "CreateContent"))
            }
          }
        }
        case Failure(t) => ret ! t
      }
    }) (sender)

    case sep: SessionEventPackage => handleEvents(sep)
  }
}
