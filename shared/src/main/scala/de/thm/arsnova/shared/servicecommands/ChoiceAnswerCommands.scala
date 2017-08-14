package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.ChoiceAnswer

object ChoiceAnswerCommands {
  sealed trait ChoiceAnswerCommand extends ServiceCommand {
    def questionId: UUID
  }

  case class GetChoiceAnswers(questionId: UUID) extends ChoiceAnswerCommand

  case class GetChoiceAnswer(questionId: UUID, id: UUID) extends ChoiceAnswerCommand

  case class CreateChoiceAnswer(questionId: UUID, answer: ChoiceAnswer) extends ChoiceAnswerCommand
}
