package de.thm.arsnova.commentservice

import akka.actor.{Props}

import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.management.RegistryCommands.RegisterService

object CommentService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
  val managerProps = Props(classOf[ServiceManagementActor], "comment", dispatcher)
  val manager = system.actorOf(managerProps, name = "manager")
}