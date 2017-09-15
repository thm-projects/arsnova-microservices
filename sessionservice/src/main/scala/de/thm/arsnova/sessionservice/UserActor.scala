package de.thm.arsnova.sessionservice

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.persistence.PersistentActor
import akka.util.Timeout
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.shared.Exceptions.{InvalidToken, ResourceNotFound}
import de.thm.arsnova.shared.entities.{Session, SessionRole, User}
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.events.SessionEvents.{SessionCreated, SessionDeleted}
import de.thm.arsnova.shared.events.UserEvents.{UserCreated, UserGetsSessionRole, UserLosesSessionRole}
import de.thm.arsnova.shared.servicecommands.SessionCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.shards.SessionShard

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object UserActor {
  def props(): Props =
    Props(new UserActor())
}

class UserActor() extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val sessionRegion = ClusterSharding(context.system).shardRegion(SessionShard.shardName)

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var userState: Option[User] = None
  private val rolesState: collection.mutable.Set[SessionRole] = collection.mutable.Set.empty[SessionRole]

  override def receiveRecover: Receive = {
    case UserCreated(user) =>
      userState = Some(user)
      context.become(userCreated)
    case UserGetsSessionRole(role) =>
      rolesState += role
    case UserLosesSessionRole(role) =>
      rolesState -= role
  }

  override def receiveCommand: Receive = initial

  def handleSessionEvents(sep: SessionEventPackage) = {
    sep.event match {
      case SessionCreated(session) => {
        val newRole = SessionRole(session.userId, session.id.get, "owner")
        rolesState += newRole
        persist(UserGetsSessionRole(newRole)) { e => e}
      }
      case SessionDeleted(session) => {
        val oldRole = SessionRole(session.userId, session.id.get, "owner")
        rolesState -= oldRole
        persist(UserLosesSessionRole(oldRole))(e => e)
      }
    }
  }

  def initial: Receive = {
    case CreateUser(userId, user) => ((ret: ActorRef) => {
      ret ! user
      userState = Some(user)
      context.become(userCreated)
      persist(UserCreated(user))(e => e)
    }) (sender)
    case GetUser(userId) => {
      sender() ! Failure(ResourceNotFound("user"))
    }
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
        case None => ret ! "guest"
      }
    }) (sender)
    case GetUserSessions(userId, withRole) => ((ret: ActorRef) => {
      val roles: Seq[SessionRole] = withRole match {
        case Some(r) => {
          rolesState.filter(_.role == r).toSeq
        }
        case None => {
          rolesState.toSeq
        }
      }
      val askFutures: Seq[Future[Option[Session]]] = roles map { sr =>
        (sessionRegion ? GetSession(sr.sessionId)).mapTo[Try[Session]].map {
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
    }) (sender)
    case sep: SessionEventPackage => handleSessionEvents(sep)
  }
}
