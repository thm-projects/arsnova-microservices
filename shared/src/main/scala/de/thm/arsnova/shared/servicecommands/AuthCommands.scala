package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import de.thm.arsnova.shared.entities.{Token, User}

object AuthCommands {
  sealed trait AuthCommand extends ServiceCommand

  case class LoginUser(username: String, password: String) extends AuthCommand

  case class GetUserFromTokenString(tokenstring: String) extends AuthCommand

  case class CheckTokenString(tokenstring: String) extends AuthCommand
}
