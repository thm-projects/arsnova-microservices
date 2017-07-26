package de.thm.arsnova.authservice


import scala.concurrent.duration._
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
import akka.actor.{Props, ActorRef}
import akka.pattern.pipe
import de.thm.arsnova.authservice.repositories.{SessionRoleRepository, UserRepository}
import de.thm.arsnova.shared.entities.{SessionRole, User}
import de.thm.arsnova.shared.events.UserEvents.{UserCreated, UserGetsSessionRole}
import de.thm.arsnova.shared.servicecommands.UserCommands._

import scala.concurrent.ExecutionContext

object UserActor {
  val shardName = "User"

  def props(): Props =
    Props(new UserActor())

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: UserCommand => (cmd.userId.toString, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: UserCommand => math.abs(cmd.userId.hashCode() % 100).toString
  }
}

class UserActor extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher

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

  override def receiveCommand: Receive = {
    case GetUser(userId) => ((ret: ActorRef) => {
      userState match {
        case Some(u) => ret ! Some(u)
        case None => UserRepository.findById(userId) map {
          case Some(user) => {
            ret ! Some(user)
            userState = Some(user)
          }
          case None =>
            ret ! None
        }
      }
    }) (sender)
    case CreateUser(userId, user) => ((ret: ActorRef) => {
      UserRepository.create(user) map { u =>
        ret ! u
        userState = Some(u)
        persist(UserCreated(u))(e => e)
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
  }
}
