package de.thm.arsnova.sessionservice

import akka.actor.{Props}

import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.management.RegistryCommands.RegisterService

object SessionService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
  val managerProps = Props(classOf[ServiceManagementActor], "session", dispatcher)
  val manager = system.actorOf(managerProps, name = "manager")
}