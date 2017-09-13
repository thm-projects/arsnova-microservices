package de.thm.arsnova.sessionservice

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.persistence.PersistentActor
import akka.util.Timeout
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.shared.Exceptions._
import de.thm.arsnova.shared.entities.{Content, Session, User}
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.events.SessionEvents.{SessionCreated, SessionDeleted}
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.servicecommands.SessionCommands.GetSession
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.shards.{EventShard, SessionShard, UserShard}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object ContentListActor {
  def props(authRouter: ActorRef): Props =
    Props(new ContentListActor(authRouter: ActorRef))
}

class ContentListActor(authRouter: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val eventRegion = ClusterSharding(context.system).shardRegion(EventShard.shardName)

  val userRegion = ClusterSharding(context.system).shardRegion(UserShard.shardName)

  val sessionRegion = ClusterSharding(context.system).shardRegion(SessionShard.shardName)

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
      (sessionRegion ? GetSession(cmd.sessionId)).mapTo[Try[Session]] map {
        case Success(session) => {
          context.become(sessionCreated)
          context.self ! cmd
          persist(SessionCreated(session))(e => e)
        }
        case Failure(t) => sender() ! Failure(NoSuchSession(Left(cmd.sessionId)))
      }
    }
  }

  def sessionCreated: Receive = {
    case GetContent(sessionid, id) => ((ret: ActorRef) => {
      contentlist.get(id) match {
        case Some(c) => ret ! Some(c)
        case None => ret ! None
      }
    }) (sender)
    case DeleteContent(sessionId, id, userId) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForSession(userId, id)).mapTo[String] map { role =>
        if (role == "owner") {
          val c = contentlist.remove(id)
          ret ! Success(c.get)
          eventRegion ! SessionEventPackage(sessionId, ContentDeleted(c.get))
          persist(ContentDeleted(c.get))(e => e)
        } else {
          ret ! Failure(InsufficientRights(role, "Delete Content"))
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
    case CreateContent(sessionid, content, userID) => ((ret: ActorRef) => {
      (userRegion ? GetRoleForSession(userID, sessionid)).mapTo[String] map { role =>
        if (role != "guest") {
          contentlist += content.id.get -> content
          ret ! Success(content)
          eventRegion ! SessionEventPackage(content.sessionId, ContentCreated(content))
          persist(ContentCreated(content)) { e => e }
        } else {
          ret ! Failure(InsufficientRights(role, "CreateContent"))
        }
      }
    }) (sender)

    case sep: SessionEventPackage => handleEvents(sep)
  }
}
