package de.thm.arsnova.commentservice

import akka.actor.{Props}

import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.management.RegistryCommands.RegisterService

object CommentService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
  val manager = system.actorOf(Props[ServiceManagementActor], name = "manager")

  manager ! RegisterService("CommentService", dispatcher)
}