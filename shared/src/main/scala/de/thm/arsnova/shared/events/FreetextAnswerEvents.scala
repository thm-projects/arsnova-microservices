package de.thm.arsnova.shared.events

import java.util.UUID

import de.thm.arsnova.shared.entities.FreetextAnswer

object FreetextAnswerEvents {
  trait FreetextAnswerEvent extends ServiceEvent

  case class FreetextAnswerCreated(answer: FreetextAnswer) extends FreetextAnswerEvent
}
