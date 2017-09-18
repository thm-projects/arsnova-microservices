package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import de.thm.arsnova.shared.entities.Room

object RoomJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  implicit val roomFormat: RootJsonFormat[Room] = jsonFormat10(Room)
}
