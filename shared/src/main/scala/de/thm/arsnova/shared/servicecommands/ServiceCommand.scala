package de.thm.arsnova.shared.servicecommands

import de.thm.arsnova.shared.entities.User

trait ServiceCommand

case class CommandWithToken(serviceCommand: ServiceCommand, token: Option[String])
