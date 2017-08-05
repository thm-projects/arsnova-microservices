package de.thm.arsnova.shared.servicecommands

import java.util.UUID

object EventCommands {
  trait EventCommand extends ServiceCommand {
    def id: UUID
  }

  case class Sub(id: UUID, eventName: String) extends EventCommand

  case class UnSub(id: UUID, eventName: String) extends EventCommand
}
