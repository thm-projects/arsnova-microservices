package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.actor.ActorRef
import de.thm.arsnova.shared.entities.Content
import de.thm.arsnova.shared.entities.export.ContentExport

object ContentCommands {
  sealed trait ContentCommand extends ServiceCommand {
    def id: UUID
  }

  case class ContentCommandWithRole(cmd: ContentCommand, role: String, ret: ActorRef)

  case class GetContent(id: UUID) extends ContentCommand

  case class CreateContent(id: UUID, content: Content, userId: UUID) extends ContentCommand

  case class DeleteContent(id: UUID, userId: UUID) extends ContentCommand

  case class GetExport(id: UUID) extends ContentCommand

  case class Import(id: UUID, exportedContent: ContentExport) extends ContentCommand
}
