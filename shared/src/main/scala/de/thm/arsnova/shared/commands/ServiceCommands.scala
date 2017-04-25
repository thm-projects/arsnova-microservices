package de.thm.arsnova.shared.commands

object ServiceCommands {
  sealed trait ServiceCommands[R] extends Command[R]

  case class RegisterService(remotePath: String) extends ServiceCommands[Boolean]
}
