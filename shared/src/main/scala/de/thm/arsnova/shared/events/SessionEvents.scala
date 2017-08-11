package de.thm.arsnova.shared.events

import java.util.UUID

import de.thm.arsnova.shared.entities.Session

object SessionEvents {
  trait SessionEvent extends ServiceEvent

  case class SessionCreated(session: Session) extends SessionEvent

  case class SessionUpdated(session: Session) extends SessionEvent

  case class SessionDeleted(session: Session) extends SessionEvent
}
