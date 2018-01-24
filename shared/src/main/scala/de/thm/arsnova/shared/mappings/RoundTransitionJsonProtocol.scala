package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.RoundTransition

object RoundTransitionJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  implicit val roundTransitionFormat: RootJsonFormat[RoundTransition] = jsonFormat5(RoundTransition)
}
