package de.thm.arsnova.shared.events

import de.thm.arsnova.shared.entities.{User, RoomRole}

object UserEvents {
  trait UserEvent extends ServiceEvent

  case class UserCreated(user: User) extends UserEvent

  case class UserGetsRoomRole(roomRole: RoomRole) extends UserEvent

  case class UserLosesRoomRole(roomRole: RoomRole) extends UserEvent
}
