package de.thm.arsnova.shared.events

import de.thm.arsnova.shared.entities.{ChoiceAnswer, AnswerOption}

object ChoiceAnswerEvents {
  trait ChoiceAnswerEvent extends ServiceEvent

  case class ChoiceAnswerCreated(answer: ChoiceAnswer) extends ChoiceAnswerEvent

  case class ChoiceAnswersCreated(answers: Seq[ChoiceAnswer]) extends ChoiceAnswerEvent

  case class ChoiceAnswerDeleted(answer: ChoiceAnswer) extends ChoiceAnswerEvent
}
