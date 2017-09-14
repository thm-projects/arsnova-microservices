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
import akka.cluster.sharding.ClusterSharding
import akka.cluster.sharding.ShardRegion.Passivate
import akka.persistence.PersistentActor
import de.thm.arsnova.shared.entities.{Session, User}
import de.thm.arsnova.shared.events.SessionEvents.{SessionCreated, SessionDeleted, SessionEvent, SessionUpdated}
import de.thm.arsnova.shared.servicecommands.SessionCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.Exceptions
import de.thm.arsnova.shared.Exceptions.{InsufficientRights, NoSuchSession, NoUserException}
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.shards.{EventShard, UserShard}

import scala.concurrent.{ExecutionContext, Future}

object SessionActor {
  def props(authRouter: ActorRef): Props =
    Props(new SessionActor(authRouter: ActorRef))
}

class SessionActor(authRouter: ActorRef) extends PersistentActor {

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val eventRegion = ClusterSharding(context.system).shardRegion(EventShard.shardName)

  val userRegion = ClusterSharding(context.system).shardRegion(UserShard.shardName)

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var state: Option[Session] = None

  override def receiveRecover: Receive = {
    case SessionCreated(session) => {
      state = Some(session)
      context.become(sessionCreated)
    }
    case SessionUpdated(session) => {
      state = Some(session)
    }
    case SessionDeleted(session) => {
      state = None
      context.become(initial)
    }
    case s: Any => println(s)
  }

  override def receiveCommand: Receive = initial

  def initial: Receive = {
    case GetSession(id) => ((ret: ActorRef) => {
      ret ! Failure(NoSuchSession(Left(id)))
    }) (sender)
    case CreateSession(id, session, userId) => ((ret: ActorRef) => {
      state = Some(session)
      context.become(sessionCreated)
      ret ! Success(session)
      eventRegion ! SessionEventPackage(id, SessionCreated(session))
      persist(SessionCreated(session))(e => println(e))
    }) (sender)
  }

  def sessionCreated: Receive = {
    case GetSession(id) => ((ret: ActorRef) => {
      state match {
        case Some(session) => ret ! Success(session)
        case None => ret ! Failure(NoSuchSession(Left(id)))
      }
    }) (sender)
    case UpdateSession(id, session, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForSession(userId, id)).mapTo[String] map { role =>
        if (role == "owner") {
          state = Some(session)
          ret ! Success(session)
          eventRegion ! SessionEventPackage(id, SessionUpdated(session))
          persist(SessionUpdated(session))(e => println(e))
        } else {
          ret ! Failure(InsufficientRights(role, "Update Session"))
        }
      }
    }) (sender)
    case DeleteSession(id, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForSession(userId, id)).mapTo[String] map { role =>
        if (role == "owner") {
          ret ! Success(state.get)
          val e = SessionDeleted(state.get)
          state = None
          context.become(initial)
          eventRegion ! SessionEventPackage(id, e)
          persist(e)(e => e)
        } else {
          ret ! Failure(InsufficientRights(role, "Update Session"))
        }
      }
    }) (sender)
  }
}
