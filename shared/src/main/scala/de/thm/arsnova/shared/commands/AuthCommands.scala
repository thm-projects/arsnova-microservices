package de.thm.arsnova.shared.commands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.{Token, User}

object AuthCommands {
  sealed trait AuthCommand[R]

  case class LoginUser(username: String, password: String) extends AuthCommand[String]

  case class CreateUser(user: User) extends AuthCommand[Done]

  case class CheckTokenString(tokenstring: String) extends AuthCommand[Boolean]
}
