package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.FreetextAnswer

object FreetextAnswerCommands {
  sealed trait FreetextAnswerCommand extends ServiceCommand {
    def sessionId: UUID
    def questionId: UUID
  }

  case class GetFreetextAnswers(sessionId: UUID, questionId: UUID) extends FreetextAnswerCommand

  case class GetFreetextAnswer(sessionId: UUID, questionId: UUID, id: UUID) extends FreetextAnswerCommand

  case class CreateFreetextAnswer(sessionId: UUID, questionId: UUID, answer: FreetextAnswer, userId: UUID) extends FreetextAnswerCommand

  case class DeleteFreetextAnswer(sessionId: UUID, questionId: UUID, id: UUID, userId: UUID) extends FreetextAnswerCommand
}
