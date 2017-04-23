package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.User

object UserJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  implicit val commentFormat: RootJsonFormat[User] = jsonFormat3(User)
}
