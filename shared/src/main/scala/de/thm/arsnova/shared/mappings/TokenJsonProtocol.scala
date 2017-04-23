package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.Token

object TokenJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  implicit val commentFormat: RootJsonFormat[Token] = jsonFormat5(Token)
}
