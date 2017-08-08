package de.thm.arsnova.eventservice

import java.util.UUID

import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import akka.persistence.PersistentActor
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.actor.Props
import akka.actor.ActorRef
import de.thm.arsnova.shared.events.SessionEvents.SessionCreated
import de.thm.arsnova.shared.events.{ServiceEvent, SessionEventPackage}
import de.thm.arsnova.shared.servicecommands.EventCommands._

import scala.concurrent.ExecutionContext

object SessionEventActor {
  def props(): Props = {
    Props(new SessionEventActor())
  }
}

class SessionEventActor extends PersistentActor {
  import ShardRegions._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case sep: SessionEventPackage => {
      BasicEventRouting.broadcast(sep)
    }
  }
}
