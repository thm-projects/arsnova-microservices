package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.AnswerOption

object AnswerOptionJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  implicit val answerOptionFormat: RootJsonFormat[AnswerOption] = jsonFormat5(AnswerOption.apply)
}
