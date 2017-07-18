package de.thm.arsnova.sessionservice

import java.util.UUID

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
import de.thm.arsnova.shared.Exceptions
import de.thm.arsnova.shared.Exceptions.NoUserException

import scala.concurrent.{ExecutionContext, Future}

object SessionActor {
  val shardName = "Session"

  def props(authRouter: ActorRef, sessionList: ActorRef): Props =
    Props(new SessionActor(authRouter: ActorRef, sessionList: ActorRef))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: SessionCommand => (cmd.id.toString, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: SessionCommand => math.abs(cmd.id.hashCode() % 100).toString
  }
}

class SessionActor(authRouter: ActorRef, sessionList: ActorRef) extends PersistentActor {

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  def tokenToUser(tokenstring: String): Future[Option[User]] = {
    (authRouter ? GetUserFromTokenString(tokenstring)).mapTo[Option[User]]
  }

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var state: Option[Session] = None

  override def receiveRecover: Receive = {
    case event: SessionEvent =>
      println(event)
  }

  override def receiveCommand: Receive = initial

  def initial: Receive = {
    case GetSession(id) => ((ret: ActorRef) => {
      SessionRepository.findById(id) map { session =>
        state = Some(session)
        context.become(created)
        ret ! session
      }
    }) (sender)
    case CreateSession(id, session, token) => ((ret: ActorRef) => {
      token match {
        case Some(t) => tokenToUser(t) map { user =>
          (sessionList ? GenerateKeyword(session.id.get)).mapTo[NewKeyword].map { newKeyword =>
            val s = session.copy(keyword = Some(newKeyword.keyword))
            SessionRepository.create(session, user) map { s =>
              state = Some(session)
              context.become(created)
              ret ! session
              persistAsync(SessionCreated(session))(_)
            }
          }
        }
        case None => ret ! NoUserException
      }
    }) (sender)
  }

  def created: Receive = {
    case GetSession(id) => ((ret: ActorRef) => {
      SessionRepository.findById(id) map { session =>
        state = Some(session)
        context.become(created)
        ret ! session
      }
    }) (sender)
  }
}
