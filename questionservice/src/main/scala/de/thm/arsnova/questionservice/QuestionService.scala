package de.thm.arsnova.questionservice

import akka.actor.{Props}

import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.management.RegistryCommands.RegisterService

object QuestionService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
  val managerProps = Props(classOf[ServiceManagementActor], "question", dispatcher)
  val manager = system.actorOf(managerProps, name = "manager")
}