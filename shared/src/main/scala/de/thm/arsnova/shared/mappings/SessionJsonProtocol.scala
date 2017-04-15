package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import de.thm.arsnova.shared.entities.Session

object SessionJsonProtocol extends DefaultJsonProtocol {
  import de.thm.arsnova.shared.mappings.UUIDProtocol._
  implicit val sessionFormat: RootJsonFormat[Session] = jsonFormat10(Session)
}
