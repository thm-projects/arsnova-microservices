package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.FreetextAnswer

object FreetextAnswerCommands {
  sealed trait FreetextAnswerCommand extends ServiceCommand {
    def questionId: UUID
  }

  case class GetFreetextAnswers(questionId: UUID) extends FreetextAnswerCommand

  case class GetFreetextAnswer(questionId: UUID, id: UUID) extends FreetextAnswerCommand

  case class CreateFreetextAnswer(questionId: UUID, answer: FreetextAnswer) extends FreetextAnswerCommand
}
