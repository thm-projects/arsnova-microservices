package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.Content

object ContentCommands {
  sealed trait ContentCommand extends ServiceCommand {
    def sessionid: UUID
  }

  case class GetContent(sessionid: UUID, id: UUID) extends ContentCommand

  case class GetContentListBySessionId(sessionid: UUID) extends ContentCommand

  case class GetContentListBySessionIdAndVariant(sessionid: UUID, variant: String) extends ContentCommand

  case class CreateContent(sessionid: UUID, content: Content, token: Option[String]) extends ContentCommand
}
