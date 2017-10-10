package de.thm.arsnova.shared.events

import java.util.UUID

import de.thm.arsnova.shared.entities.Room

object RoomEvents {
  trait RoomEvent extends ServiceEvent

  case class RoomCreated(room: Room) extends RoomEvent

  case class RoomUpdated(room: Room) extends RoomEvent

  case class RoomDeleted(room: Room) extends RoomEvent

  case class RoomContentAdded(id: UUID, group: String) extends RoomEvent

  case class RoomContentDeleted(id: UUID, group: String) extends RoomEvent
}
