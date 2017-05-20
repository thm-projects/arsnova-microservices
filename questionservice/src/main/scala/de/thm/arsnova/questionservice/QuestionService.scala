package de.thm.arsnova.questionservice

import akka.actor.{Props}

import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.management.RegistryCommands.RegisterService

object QuestionService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
  val manager = system.actorOf(Props[ServiceManagementActor], name = "manager")

  manager ! RegisterService("QuestionService", dispatcher)
}