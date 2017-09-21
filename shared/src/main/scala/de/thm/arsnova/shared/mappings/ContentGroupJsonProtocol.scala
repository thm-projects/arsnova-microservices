package de.thm.arsnova.shared.mappings

import de.thm.arsnova.shared.entities.ContentGroup
import java.util.UUID
import spray.json._

object ContentGroupJsonProtocol extends UUIDFormat {
  implicit object ContentGroupObject extends JsonFormat[ContentGroup] {
    def write(contentGroup: ContentGroup): JsValue = {
      val contentIds = contentGroup.contentIds.map(s => Format.write(s))
      JsObject(
        contentGroup.name -> JsObject(
          "autoSort" -> JsBoolean(contentGroup.autoSort),
          "contentIds" -> contentIds
        )
      )
    }

    def read(value: JsValue) = {

    }
  }
}
