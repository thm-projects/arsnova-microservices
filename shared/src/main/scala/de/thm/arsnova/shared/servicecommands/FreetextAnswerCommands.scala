package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import akka.actor.ActorRef
import de.thm.arsnova.shared.entities.FreetextAnswer
import de.thm.arsnova.shared.entities.export.FreetextAnswerExport

object FreetextAnswerCommands {
  sealed trait FreetextAnswerCommand extends ServiceCommand {
    def roomId: UUID
    def contentId: UUID
  }

  case class FreetextAnswerCommandWithRole(cmd: FreetextAnswerCommand, role: String, ret: ActorRef)

  case class GetFreetextAnswers(roomId: UUID, contentId: UUID) extends FreetextAnswerCommand

  case class GetFreetextAnswer(roomId: UUID, contentId: UUID, id: UUID) extends FreetextAnswerCommand

  case class CreateFreetextAnswer(roomId: UUID, contentId: UUID, answer: FreetextAnswer, userId: UUID) extends FreetextAnswerCommand

  case class DeleteFreetextAnswer(roomId: UUID, contentId: UUID, id: UUID, userId: UUID) extends FreetextAnswerCommand

  case class GetFreetextStatistics(roomId: UUID, contentId: UUID) extends FreetextAnswerCommand

  case class ImportFreetextAnswers(roomId: UUID, contentId: UUID, exportedAnswers: Seq[FreetextAnswerExport]) extends FreetextAnswerCommand
}
