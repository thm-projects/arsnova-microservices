package de.thm.arsnova.shared.events

import de.thm.arsnova.shared.entities.Session

object SessionEvents {
  trait SessionEvent

  case class SessionCreated(session: Session) extends SessionEvent
}
