package de.thm.arsnova.shared.management

import akka.actor.ActorRef

object RegistryCommands {
  sealed trait RegistryCommand

  case class RegisterService(serviceType: String, remote: ActorRef) extends RegistryCommand

  case class UnregisterService(serviceType: String, remote: ActorRef) extends RegistryCommand
}
