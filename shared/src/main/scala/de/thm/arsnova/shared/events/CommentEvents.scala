package de.thm.arsnova.shared.events

import de.thm.arsnova.shared.entities.Comment

object CommentEvents {
  trait CommentEvent extends ServiceEvent

  case class CommentCreated(comment: Comment) extends CommentEvent

  case class CommentDeleted(comment: Comment) extends CommentEvent
}
