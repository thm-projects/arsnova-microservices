package de.thm.arsnova.shared.events

import de.thm.arsnova.shared.entities.ChoiceAnswer

object ChoiceAnswerEvents {
  trait ChoiceAnswerEvent extends ServiceEvent

  case class ChoiceAnswerCreated(answer: ChoiceAnswer) extends ChoiceAnswerEvent

  case class ChoiceAnswerDeleted(answer: ChoiceAnswer) extends ChoiceAnswerEvent
}
