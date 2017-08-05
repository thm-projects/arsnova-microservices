package de.thm.arsnova.eventservice

import java.util.UUID

import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import akka.persistence.PersistentActor
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.actor.Props
import akka.actor.ActorRef
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.servicecommands.EventCommands._

import scala.concurrent.ExecutionContext

object SessionEventActor {
  def props(): Props = {
    Props(new SessionEventActor())
  }
}

class SessionEventActor extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  // (sessionid, eventname) -> ref
  private val subs: collection.mutable.HashMap[(UUID, String), ActorRef] =
    collection.mutable.HashMap.empty[(UUID, String), ActorRef]

  def broadcast(sep: SessionEventPackage) = {
    subs.filter(_ == (sep.id, sep.event.getClass.toString)) foreach { sub =>
      sub._2 ! sep.event
    }
  }

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case Sub(id, eventName) => subs += (id, eventName) -> sender()
    case UnSub(id, eventName) => subs -= ((id, eventName))
    case sep: SessionEventPackage => broadcast(sep)
  }
}
