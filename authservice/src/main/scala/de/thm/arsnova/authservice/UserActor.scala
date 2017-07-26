package de.thm.arsnova.authservice


import scala.concurrent.duration._
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
import akka.remote.ContainerFormats.ActorRef
import akka.actor.Props
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

  override def receiveCommand: Receive = start

  def start: Receive = {
    case CreateUser(userId, user) => ((ret: ActorRef) => {
      UserRepository.create(user) map { u =>
        ret ! u
        userState = Some(u)
        persist(UserCreated(u))
        context.become(created)
      }
    }) (sender)
  }

  def created: Receive = {
    case GetUser(userId) =>
      sender ! userState.get
    case MakeUserOwner(userId, sessionId) => {
      val newRole = SessionRole(userId, sessionId, "owner")
      rolesState += newRole
      SessionRoleRepository.addSessionRole(newRole)
      persist(UserGetsSessionRole(newRole))
    }
    case GetRoleForSession(userId, sessionId) => {
      // need to ensure that there is only one role set per user to use find and not filter
      rolesState.find(r => r.userId == userId && r.sessionId == sessionId) match {
        case Some(r) => sender() ! r.role
        case None => "guest"
      }
    }
  }
}
