package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.Content

object ContentCommands {
  sealed trait ContentCommand extends ServiceCommand {
    def id: UUID
  }

  case class GetContent(id: UUID) extends ContentCommand

  case class CreateContent(id: UUID, content: Content, userId: UUID) extends ContentCommand

  case class DeleteContent(id: UUID, userId: UUID) extends ContentCommand
}
