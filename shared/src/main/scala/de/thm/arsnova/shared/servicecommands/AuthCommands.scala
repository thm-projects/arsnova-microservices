package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import de.thm.arsnova.shared.entities.{Token, DbUser}

object AuthCommands {
  sealed trait AuthCommand extends ServiceCommand

  case class LoginUser(username: String, password: String) extends AuthCommand

  case class AuthenticateUser(token: String) extends AuthCommand

  case class CheckTokenString(tokenstring: String) extends AuthCommand

  case class AddDbUser(user: DbUser) extends AuthCommand
}
