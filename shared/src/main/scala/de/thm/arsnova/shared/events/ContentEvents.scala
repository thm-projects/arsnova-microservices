package de.thm.arsnova.shared.events

import de.thm.arsnova.shared.entities.Content

object ContentEvents {
  trait ContentEvent extends ServiceEvent

  case class ContentCreated(content: Content) extends ContentEvent

  case class ContentDeleted(content: Content) extends ContentEvent
}
