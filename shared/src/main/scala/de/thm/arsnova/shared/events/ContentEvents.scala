package de.thm.arsnova.shared.events

import java.util.UUID

import de.thm.arsnova.shared.entities.Content

object ContentEvents {
  trait ContentEvent extends ServiceEvent

  case class ContentCreated(content: Content) extends ContentEvent

  case class ContentRefresh(content: Content) extends ContentEvent

  case class ContentDeleted(content: Content) extends ContentEvent

  case class ContentUpdated(content: Content) extends ContentEvent

  case class NewRound(contentId: UUID, round: Int) extends ContentEvent
}
