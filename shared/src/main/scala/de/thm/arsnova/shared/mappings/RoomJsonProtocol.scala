package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import de.thm.arsnova.shared.entities.Room

object RoomJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  import ContentGroupJsonProtocol._
  implicit val roomFormat: RootJsonFormat[Room] = jsonFormat11(Room.apply)
}
