package de.thm.arsnova.shared.mappings

import de.thm.arsnova.shared.entities.ContentGroup
import spray.json._

object ContentGroupJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  implicit val contentGroupFormat: RootJsonFormat[ContentGroup] = jsonFormat2(ContentGroup)
}
