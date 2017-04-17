package de.thm.arsnova.shared.mappings

import spray.json._
import de.thm.arsnova.shared.entities.FormatAttributes

/*
 FormatAttributes JSON protocol must be done manually since the map isn't serialized in a "spray.json default way"
 */
/*object FormatAttributesJsonProtocol extends RootJsonFormat[FormatAttributes] with DefaultJsonProtocol {
  def write(fA: FormatAttributes): JsValue = {
    val keyVals = fA.attributes.map {
      case (key, value) => key -> JsString(value)
    }
    JsObject(keyVals)
  }

  def read(json: JsValue): FormatAttributes = {
    json match {
      case js: JsObject => {
        val wat: Map[String, JsValue] = json.asJsObject.fields
        val attributes = wat.map {
          case (key, value) => key -> value.convertTo[String]
        }
        FormatAttributes(attributes = attributes)
      }
      case _ => FormatAttributes(Map())
    }
  }
}*/