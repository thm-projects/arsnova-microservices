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
import de.thm.arsnova.shared.entities.{Question, User}
import de.thm.arsnova.shared.events.QuestionEvents._
import de.thm.arsnova.shared.servicecommands.QuestionCommands._
import de.thm.arsnova.shared.Exceptions._
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString

import scala.concurrent.{ExecutionContext, Future}

object QuestionListActor {
  val shardName = "Question"

  def props(authRouter: ActorRef): Props =
    Props(new QuestionListActor(authRouter: ActorRef))

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: QuestionCommand => (cmd.sessionid.toString, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: QuestionCommand => math.abs(cmd.sessionid.hashCode() % 100).toString
  }
}

class QuestionListActor(authRouter: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var state: Option[Seq[Question]] = None

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
    case GetQuestion(sessionid, id) => ((ret: ActorRef) => {

    }) (sender)
  }

  def created: Receive = {

  }
}
