package de.thm.arsnova.contentservice

import java.util.UUID

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
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
import de.thm.arsnova.contentservice.repositories.ContentRepository
import de.thm.arsnova.shared.entities.{Content, User}
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.Exceptions._
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString

import scala.concurrent.{ExecutionContext, Future}

object ContentListActor {
  val shardName = "Question"

  def props(authRouter: ActorRef, userRegion: ActorRef): Props =
    Props(new ContentListActor(authRouter: ActorRef, userRegion: ActorRef))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: ContentCommand => (cmd.sessionid.toString, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: ContentCommand => math.abs(cmd.sessionid.hashCode() % 100).toString
  }
}

class ContentListActor(authRouter: ActorRef, userRegion: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private val contentlist: collection.mutable.HashMap[UUID, Content] =
    collection.mutable.HashMap.empty[UUID, Content]

  def tokenToUser(tokenstring: String): Future[Option[User]] = {
    (authRouter ? GetUserFromTokenString(tokenstring)).mapTo[Option[User]]
  }

  override def persistenceId: String = self.path.parent.name + "-"  + self.path.name

  override def receiveRecover: Receive = {
    case ContentCreated(c) =>
      contentlist += c.id.get -> c
  }

  override def receiveCommand: Receive = {
    case GetContent(sessionid, id) => ((ret: ActorRef) => {
      contentlist.get(id) match {
        case Some(c) => ret ! Some(c)
        case None =>
          ContentRepository.findById(id) map {
            case Some(c) => {
              contentlist += id -> c
              ret ! Some(c)
            }
            case None => ret ! None
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
    case CreateContent(sessionid, content, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Some(user) => {
          (userRegion ? GetRoleForSession(user.id.get, sessionid)).mapTo[String] map { role =>
            if (role != "guest") {
              ContentRepository.create(content) map { c =>
                contentlist += c.id.get -> c
                ret ! Success(c)
                persist(ContentCreated(c)) { e => e }
              }
            } else {
              ret ! Failure(InsufficientRights(role, "CreateContent"))
            }
          }
        }
        case None => ret ! Failure(NoUserException("CreateContent"))
      }
    }) (sender)
  }
}
