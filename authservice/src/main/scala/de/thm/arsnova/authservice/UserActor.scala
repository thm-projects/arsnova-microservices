package de.thm.arsnova.authservice


import scala.concurrent.duration._
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
import akka.remote.ContainerFormats.ActorRef
import akka.actor.Props
import de.thm.arsnova.authservice.repositories.{SessionRoleRepository, UserRepository}
import de.thm.arsnova.shared.entities.{User, SessionRole}
import de.thm.arsnova.shared.events.UserEvents.UserCreated
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

  private var state: Option[User] = None

  override def receiveRecover: Receive = {
    case UserCreated(user) =>
      state = Some(user)
  }

  override def receiveCommand: Receive = start

  def start: Receive = {
    case CreateUser(userId, user) => ((ret: ActorRef) => {
      UserRepository.create(user) map { u =>
        ret ! u
        state = Some(u)
        persist(UserCreated(u))
        context.become(created)
      }
    }) (sender)
  }

  def created: Receive = {
    case GetUser(userId) =>
      sender ! state.get
    case MakeUserOwner(userId, sessionId) => {
      SessionRoleRepository.addSessionRole(SessionRole(userId, sessionId, "owner"))
    }
  }
}
