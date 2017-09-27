package de.thm.arsnova.shared.mappings

import de.thm.arsnova.shared.entities.ContentGroup
import java.util.UUID
import spray.json._

object ContentGroupJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  implicit object ContentGroupObject extends JsonFormat[ContentGroup] {
    def write(contentGroup: ContentGroup): JsValue = {
      JsObject(
        contentGroup.name -> JsObject(
          "autoSort" -> JsBoolean(contentGroup.autoSort),
          "contentIds" -> contentGroup.contentIds.toJson
        )
      )
    }

    def read(value: JsValue): ContentGroup = {
      value match {
        case js: JsObject => {
          val v: Map[String, JsValue] = js.fields
          val groupName = js.fields.keys.head
          js.fields.values match {
            case js: JsObject => {
              val autoSort = js.fields.get("autoSort").get.convertTo[Boolean]
              val contentIds = js.fields.get("contentIds").get.convertTo[Seq[UUID]]
              ContentGroup(groupName, autoSort, contentIds)
            }
          }
        }
      }
    }
  }
}
