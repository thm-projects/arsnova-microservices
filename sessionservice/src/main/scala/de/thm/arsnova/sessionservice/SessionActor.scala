package de.thm.arsnova.sessionservice

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
import akka.cluster.sharding.ShardRegion.Passivate
import akka.persistence.PersistentActor
import de.thm.arsnova.shared.entities.{Session, User}
import de.thm.arsnova.shared.events.SessionEvents.{SessionCreated, SessionEvent}
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.SessionCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.Exceptions
import de.thm.arsnova.shared.Exceptions.{InsufficientRights, NoSuchSession, NoUserException}
import de.thm.arsnova.shared.events.SessionEventPackage

import scala.concurrent.{ExecutionContext, Future}

object SessionActor {
  def props(eventRegion: ActorRef, authRouter: ActorRef, userRegion: ActorRef): Props =
    Props(new SessionActor(eventRegion: ActorRef, authRouter: ActorRef, userRegion: ActorRef))
}

class SessionActor(eventRegion: ActorRef, authRouter: ActorRef, userRegion: ActorRef) extends PersistentActor {

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  def tokenToUser(tokenstring: String): Future[Try[User]] = {
    (authRouter ? GetUserFromTokenString(tokenstring)).mapTo[Try[User]]
  }

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var state: Option[Session] = None

  override def receiveRecover: Receive = {
    case SessionCreated(session) => {
      state = Some(session)
      context.become(sessionCreated)
    }
    case s: Any => println(s)
  }

  override def receiveCommand: Receive = initial

  def initial: Receive = {
    case GetSession(id) => ((ret: ActorRef) => {
      ret ! Failure(NoSuchSession(Left(id)))
    }) (sender)
    case CreateSession(id, session, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Success(user) => {
          SessionRepository.create(session.copy(userId = user.id.get)) map { sRet =>
            state = Some(sRet)
            context.become(sessionCreated)
            ret ! Success(sRet)
            eventRegion ! SessionEventPackage(user.id.get, SessionCreated(sRet))
            persist(SessionCreated(sRet))(e => println(e))
          }
        }
        case Failure(t) => ret ! t
      }
    }) (sender)
  }

  def sessionCreated: Receive = {
    case GetSession(id) => ((ret: ActorRef) => {
      SessionRepository.findById(id) map {
        case Some(session) => {
          state = Some(session)
          ret ! Success(session)
        }
        case None => ret ! Failure(NoSuchSession(Left(id)))
      }
    }) (sender)
    case UpdateSession(id, session, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Success(user) => {
          (userRegion ? GetRoleForSession(user.id.get, id)).mapTo[String] map { role =>
            if (role == "owner") {
              SessionRepository.update(session) map { s =>
                state = Some(s)
                ret ! Success(s)
              }
            } else {
              ret ! Failure(InsufficientRights(role, "Update Session"))
            }
          }
        }
      }
    }) (sender)
    case DeleteSession(id, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Success(user) => {
          (userRegion ? GetRoleForSession(user.id.get, id)).mapTo[String] map { role =>
            if (role == "owner") {
              SessionRepository.delete(id) onComplete {
                case Success(i) => {
                  ret ! Success(state.get)
                  state = None
                  context.become(initial)
                }
                case Failure(t) => ret ! Failure(t)
              }
            } else {
              ret ! Failure(InsufficientRights(role, "Update Session"))
            }
          }
        }
      }
    }) (sender)
  }
}
