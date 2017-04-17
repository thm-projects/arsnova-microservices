package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.FreetextAnswer

object FreetextAnswerJsonProtocol extends DefaultJsonProtocol {
  implicit val freetextAnswerFormat: RootJsonFormat[FreetextAnswer] = jsonFormat5(FreetextAnswer)
}