package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.ChoiceAnswer

object ChoiceAnswerCommands {
  sealed trait ChoiceAnswerCommand extends ServiceCommand

  case class GetChoiceAnswer(id: UUID) extends ChoiceAnswerCommand

  case class GetChoiceAnswersByQuestionId(id: UUID) extends ChoiceAnswerCommand

  case class CreateChoiceAnswer(answer: ChoiceAnswer) extends ChoiceAnswerCommand
}
