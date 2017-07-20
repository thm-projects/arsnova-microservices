package de.thm.arsnova.questionservice

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
import de.thm.arsnova.shared.entities.{Content, User}
import de.thm.arsnova.shared.events.QuestionEvents._
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.Exceptions._
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString

import scala.concurrent.{ExecutionContext, Future}

object ContentListActor {
  val shardName = "Question"

  def props(authRouter: ActorRef): Props =
    Props(new ContentListActor(authRouter: ActorRef))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: ContentCommand => (cmd.sessionid.toString, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: ContentCommand => math.abs(cmd.sessionid.hashCode() % 100).toString
  }
}

class ContentListActor(authRouter: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var state: Option[Seq[Content]] = None

  def tokenToUser(tokenstring: String): Future[Option[User]] = {
    (authRouter ? GetUserFromTokenString(tokenstring)).mapTo[Option[User]]
  }

  override def persistenceId: String = self.path.parent.name + "-"  + self.path.name

  override def receiveRecover: Receive = {
    case event: QuestionEvent =>
      println(event)
  }

  override def receiveCommand: Receive = initial

  def initial: Receive = {
    case GetContent(sessionid, id) => ((ret: ActorRef) => {

    }) (sender)
  }

  def created: Receive = {

  }
}
