package de.thm.arsnova.sessionservice

import java.util.UUID

import scala.concurrent.duration._
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.ReceiveTimeout
import akka.pattern.{ask, pipe}
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.Passivate
import akka.persistence.PersistentActor
import de.thm.arsnova.shared.entities.{Session, User}
import de.thm.arsnova.shared.events.SessionEvents.SessionCreated
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.SessionCommands._

import scala.concurrent.Future

object SessionActor {
  val shardName = "Session"

  def props(): Props = Props(new SessionActor())

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: SessionCommand => (cmd.id.toString, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: SessionCommand => math.abs(cmd.id.hashCode() % 100).toString
  }
}

class SessionActor extends PersistentActor {

  val authRouter = context.actorSelection("/user/AuthRouter")

  def tokenToUser(tokenstring: String): Future[Option[User]] = {
    (authRouter ? GetUserFromTokenString(tokenstring)).mapTo[Option[User]]
  }

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var state: Option[Session] = None

  override def receiveRecover: Receive = {

  }

  override def receiveCommand: Receive = initial

  def initial: Receive = {
    case GetSession(id) => ((ret: ActorRef) => {
      SessionRepository.findById(id) map {
        case session: Session => {
          state = Some(session)
          context.become(created)
          ret ! session
        }
      }
    }) (sender)
    case CreateSession(id, session, token) => ((ret: ActorRef) => {
      tokenToUser(token) map { user =>
        SessionRepository.create(session, user) map {
          case session: Session => {
            persist(SessionCreated(session))
            state = Some(session)
            context.become(created)
            ret ! session.id
          }
        }
      }
    }) (sender)
  }

  def created: Receive = {
    case CommandWithToken(command, token) => command match {
      case GetSession(_) => state.get
    }
  }
}
