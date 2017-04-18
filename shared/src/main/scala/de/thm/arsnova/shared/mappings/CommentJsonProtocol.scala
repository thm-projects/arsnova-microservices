package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.Comment

object CommentJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  implicit val commentFormat: RootJsonFormat[Comment] = jsonFormat7(Comment)
}
