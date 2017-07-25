package de.thm.arsnova.shared.events

import de.thm.arsnova.shared.entities.User

object UserEvents {
  trait UserEvent

  case class UserCreated(user: User) extends UserEvent
}
