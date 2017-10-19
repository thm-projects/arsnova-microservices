package de.thm.arsnova.shared.entities.export

import de.thm.arsnova.shared.entities._

case class RoomExport(
  title: String,
  shortName: String,
  contentGroups: Map[String, ContentGroup],
  content: Seq[ContentExport],
  comments: Seq[Comment]
)

object RoomExport {
  def apply(room: Room): RoomExport =
    RoomExport(
      room.title,
      room.shortName,
      room.contentGroups,
      Nil,
      Nil
    )
}
