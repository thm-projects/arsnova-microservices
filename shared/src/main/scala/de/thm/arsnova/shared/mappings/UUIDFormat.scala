package de.thm.arsnova.shared.mappings

import java.util.UUID
import spray.json._

trait UUIDFormat {
  implicit object Format extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)

    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ => throw DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }
}
