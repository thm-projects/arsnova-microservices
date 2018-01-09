package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import akka.actor.ActorRef
import de.thm.arsnova.shared.entities.FreetextAnswer
import de.thm.arsnova.shared.entities.export.FreetextAnswerExport

object FreetextAnswerCommands {
  sealed trait FreetextAnswerCommand extends ServiceCommand {
    def roomId: UUID
    def questionId: UUID
  }

  case class FreetextAnswerCommandWithRole(cmd: FreetextAnswerCommand, role: String, ret: ActorRef)

  case class GetFreetextAnswers(roomId: UUID, questionId: UUID) extends FreetextAnswerCommand

  case class GetFreetextAnswer(roomId: UUID, questionId: UUID, id: UUID) extends FreetextAnswerCommand

  case class CreateFreetextAnswer(roomId: UUID, questionId: UUID, answer: FreetextAnswer, userId: UUID) extends FreetextAnswerCommand

  case class DeleteFreetextAnswer(roomId: UUID, questionId: UUID, id: UUID, userId: UUID) extends FreetextAnswerCommand

  case class GetFreetextStatistics(roomId: UUID, questionId: UUID) extends FreetextAnswerCommand

  case class ImportFreetextAnswers(roomId: UUID, questionId: UUID, exportedAnswers: Seq[FreetextAnswerExport]) extends FreetextAnswerCommand
}
