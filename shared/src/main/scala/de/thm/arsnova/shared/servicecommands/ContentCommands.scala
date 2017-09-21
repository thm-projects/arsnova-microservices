package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.Content

object ContentCommands {
  sealed trait ContentCommand extends ServiceCommand {
    def roomId: UUID
  }

  case class GetContent(roomId: UUID, id: UUID) extends ContentCommand

  case class GetContentListByRoomId(roomId: UUID) extends ContentCommand

  case class GetContentListByRoomIdAndGroup(roomId: UUID, group: String) extends ContentCommand

  case class CreateContent(roomId: UUID, content: Content, userId: UUID) extends ContentCommand

  case class DeleteContent(roomId: UUID, id: UUID, userId: UUID) extends ContentCommand
}
