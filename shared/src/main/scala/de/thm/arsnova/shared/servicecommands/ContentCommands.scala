package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.Content

object ContentCommands {
  sealed trait ContentCommand extends ServiceCommand {
    def sessionId: UUID
  }

  case class GetContent(sessionId: UUID, id: UUID) extends ContentCommand

  case class GetContentListBySessionId(sessionId: UUID) extends ContentCommand

  case class GetContentListBySessionIdAndVariant(sessionId: UUID, variant: String) extends ContentCommand

  case class CreateContent(sessionId: UUID, content: Content, token: String) extends ContentCommand

  case class DeleteContent(sessionId: UUID, id: UUID) extends ContentCommand
}
