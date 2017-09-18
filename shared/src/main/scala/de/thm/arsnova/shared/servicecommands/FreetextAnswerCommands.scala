package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.FreetextAnswer

object FreetextAnswerCommands {
  sealed trait FreetextAnswerCommand extends ServiceCommand {
    def roomId: UUID
    def questionId: UUID
  }

  case class GetFreetextAnswers(roomId: UUID, questionId: UUID) extends FreetextAnswerCommand

  case class GetFreetextAnswer(roomId: UUID, questionId: UUID, id: UUID) extends FreetextAnswerCommand

  case class CreateFreetextAnswer(roomId: UUID, questionId: UUID, answer: FreetextAnswer, userId: UUID) extends FreetextAnswerCommand

  case class DeleteFreetextAnswer(roomId: UUID, questionId: UUID, id: UUID, userId: UUID) extends FreetextAnswerCommand
}
