package de.thm.arsnova.eventservice

import java.util.UUID

import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import akka.persistence.PersistentActor
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.actor.Props
import akka.actor.ActorRef
import de.thm.arsnova.shared.events.RoomEvents.RoomCreated
import de.thm.arsnova.shared.events.{ServiceEvent, RoomEventPackage}
import de.thm.arsnova.shared.servicecommands.EventCommands._

import scala.concurrent.ExecutionContext

object RoomEventActor {
  def props(): Props = {
    Props(new RoomEventActor())
  }
}

class RoomEventActor extends PersistentActor {
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
    case sep: RoomEventPackage => {
      BasicEventRouting.broadcast(sep)
    }
  }
}
