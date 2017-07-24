package de.thm.arsnova.shared.events

import de.thm.arsnova.shared.entities.Content

object ContentEvents {
  trait ContentEvent

  case class ContentCreated(content: Content) extends ContentEvent
}
