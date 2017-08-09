package de.thm.arsnova.shared.events

import de.thm.arsnova.shared.entities.{User, SessionRole}

object UserEvents {
  trait UserEvent extends ServiceEvent

  case class UserCreated(user: User) extends UserEvent

  case class UserGetsSessionRole(sessionRole: SessionRole) extends UserEvent
}
