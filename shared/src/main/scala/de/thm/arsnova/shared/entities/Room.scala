package de.thm.arsnova.shared.entities

import java.util.UUID

import de.thm.arsnova.shared.entities.export.RoomExport

case class Room(
  id: Option[UUID],
  keyword: Option[String],
  userId: Option[UUID],
  contentGroups: Map[String, ContentGroup],
  title: String,
  shortName: String,
  lastOwnerActivity: String,
  creationTime: String,
  active: Boolean,
  feedbackLock: Boolean,
  flipFlashcards: Boolean
)

object Room {
  def apply(room: RoomExport): Room =
    Room(
      None,
      None,
      None,
      room.contentGroups,
      room.title,
      room.shortName,
      "now",
      "now",
      true,
      true,
      true
    )
}