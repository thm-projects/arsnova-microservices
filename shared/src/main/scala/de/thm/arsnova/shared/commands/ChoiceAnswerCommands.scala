package de.thm.arsnova.shared.commands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.ChoiceAnswer

object ChoiceAnswerCommands {
  sealed trait ChoiceAnswerCommand[R]

  case class GetChoiceAnswer(id: UUID) extends ChoiceAnswerCommand[ChoiceAnswer]

  case class GetChoiceAnswersByQuestionId(id: UUID) extends ChoiceAnswerCommand[Seq[ChoiceAnswer]]

  case class CreateChoiceAnswer(answer: ChoiceAnswer) extends ChoiceAnswerCommand[Done]
}
