package de.thm.arsnova.shared.servicecommands

import java.util.UUID

object EventCommands {
  trait EventCommand extends ServiceCommand {
    def id: UUID
  }
}
