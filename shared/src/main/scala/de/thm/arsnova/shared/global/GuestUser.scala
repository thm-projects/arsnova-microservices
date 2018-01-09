package de.thm.arsnova.shared.global

import java.util.UUID
import de.thm.arsnova.shared.entities.User

object GuestUser {
  def apply(): User = User(Some(UUID.fromString("00000000-0000-0000-0000-000000000000")), "guest", "guest")
}
