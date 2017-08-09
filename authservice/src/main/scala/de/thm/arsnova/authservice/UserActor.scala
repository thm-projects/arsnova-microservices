package de.thm.arsnova.authservice


import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
import akka.actor.{ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import de.thm.arsnova.authservice.repositories.{SessionRoleRepository, UserRepository}
import de.thm.arsnova.shared.Exceptions.{InvalidToken, ResourceNotFound}
import de.thm.arsnova.shared.entities.{Session, SessionRole, User}
import de.thm.arsnova.shared.events.SessionEvents.{SessionCreated, SessionDeleted}
import de.thm.arsnova.shared.events.UserEvents.{UserCreated, UserGetsSessionRole}
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.servicecommands.SessionCommands._

import scala.concurrent.ExecutionContext

object UserActor {
  def props(sessionShards: ActorRef): Props =
    Props(new UserActor(sessionShards: ActorRef))
}

class UserActor(sessionShards: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var userState: Option[User] = None
  private val rolesState: collection.mutable.Set[SessionRole] = collection.mutable.Set.empty[SessionRole]

  override def receiveRecover: Receive = {
    case UserCreated(user) =>
      userState = Some(user)
    case UserGetsSessionRole(role) =>
      rolesState += role
  }

  override def receiveCommand: Receive = initial

  def handleSessionEvents(sep: SessionEventPackage) = {
    sep.event match {
      case UserCreated(user) => {
        userState = Some(user)
        context.become(userCreated)
      }
      case SessionCreated(session) => {
        val newRole = SessionRole(session.userId, session.id.get, "owner")
        SessionRoleRepository.addSessionRole(newRole)
        persist(UserGetsSessionRole(newRole)) { e => e}
      }
    }
  }

  def initial: Receive = {
    case CreateUser(userId, user) => ((ret: ActorRef) => {
      UserRepository.create(user) map { u =>
        ret ! u
        userState = Some(u)
        context.become(userCreated)
        persist(UserCreated(u))(e => e)
      }
    }) (sender)
  }

  def userCreated: Receive = {
    case GetUser(userId) => ((ret: ActorRef) => {
      userState match {
        case Some(u) => ret ! Success(u)
        case None => ret ! Failure(ResourceNotFound("user"))
      }
    }) (sender)
    case GetRoleForSession(userId, sessionId) => ((ret: ActorRef) => {
      // need to ensure that there is only one role set per user to use find and not filter
      rolesState.find(r => r.userId == userId && r.sessionId == sessionId) match {
        case Some(r) => ret ! r.role
        case None => SessionRoleRepository.getSessionRole(userId, sessionId) map {
          case Some(r) => {
            rolesState += r
            ret ! r.role
          }
          case None => ret ! "guest"
        }
      }
    }) (sender)
    case GetUserSessions(userId, tokenstring, withRole) => ((ret: ActorRef) => {
      UserRepository.getUserByTokenString(tokenstring) map {
        case Some(user) => {
          if (user.id.get == userId) {
            val futureRoles: Future[Seq[SessionRole]] = withRole match {
              case Some(r) => {
                SessionRoleRepository.getAllSessionsByRole(userId, r)
              }
              case None => {
                SessionRoleRepository.getAllSessionRoles(userId)
              }
            }
            futureRoles.map { roles =>
              val askFutures: Seq[Future[Option[Session]]] = roles map { sr =>
                (sessionShards ? GetSession(sr.sessionId)).mapTo[Try[Session]].map {
                  case Success(session) => Some(session)
                  case Failure(t) => {
                    // TODO: handle dead entries in roles set / Exceptions?
                    None
                  }
                }
              }
              Future.sequence(askFutures).map { list =>
                ret ! Success(list.flatten)
              }
            }
          } else {
            ret ! Failure(InvalidToken(tokenstring))
          }
        }
        case None => ret ! Failure(InvalidToken(tokenstring))
      }
    }) (sender)
    case sep: SessionEventPackage => handleSessionEvents(sep)
  }
}
