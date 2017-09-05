package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.ChoiceAnswer

object ChoiceAnswerCommands {
  sealed trait ChoiceAnswerCommand extends ServiceCommand {
    def sessionId: UUID
    def questionId: UUID
  }

  case class GetChoiceAnswers(sessionId: UUID, questionId: UUID) extends ChoiceAnswerCommand

  case class GetChoiceAnswer(sessionId: UUID, questionId: UUID, id: UUID) extends ChoiceAnswerCommand

  case class CreateChoiceAnswer(sessionId: UUID, questionId: UUID, answer: ChoiceAnswer, token: String) extends ChoiceAnswerCommand

  case class DeleteChoiceAnswer(sessionId: UUID, questionId: UUID, id: UUID, token: String) extends ChoiceAnswerCommand
}
