package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.ChoiceAnswer

object ChoiceAnswerCommands {
  sealed trait ChoiceAnswerCommand extends ServiceCommand {
    def roomId: UUID
    def questionId: UUID
  }

  case class GetChoiceAnswers(roomId: UUID, questionId: UUID) extends ChoiceAnswerCommand

  case class GetChoiceAnswer(roomId: UUID, questionId: UUID, id: UUID) extends ChoiceAnswerCommand

  case class CreateChoiceAnswer(roomId: UUID, questionId: UUID, answer: ChoiceAnswer, userId: UUID) extends ChoiceAnswerCommand

  case class DeleteChoiceAnswer(roomId: UUID, questionId: UUID, id: UUID, userId: UUID) extends ChoiceAnswerCommand

  case class GetStatistics(roomId: UUID, questionId: UUID) extends ChoiceAnswerCommand
}
