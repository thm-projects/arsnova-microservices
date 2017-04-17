package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.ChoiceAnswer

object ChoiceAnswerJsonProtocol extends DefaultJsonProtocol {
  implicit val choiceAnswerFormat: RootJsonFormat[ChoiceAnswer] = jsonFormat4(ChoiceAnswer)
}
