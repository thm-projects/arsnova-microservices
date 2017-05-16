package de.thm.arsnova.shared.management

import akka.actor.ActorRef

import de.thm.arsnova.shared.servicecommands.ServiceCommand
import de.thm.arsnova.shared.entities.User

case class CommandPackage(command: ServiceCommand, user: Option[User], returnRef: ActorRef)
