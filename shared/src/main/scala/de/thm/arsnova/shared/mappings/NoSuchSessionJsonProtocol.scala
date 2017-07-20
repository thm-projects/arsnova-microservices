package de.thm.arsnova.shared.mappings

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import de.thm.arsnova.shared.Exceptions.NoSuchSession

object NoSuchSessionJsonProtocol extends DefaultJsonProtocol {
  implicit val noSuchSessionFormat: RootJsonFormat[NoSuchSession] = jsonFormat1(NoSuchSession)
}
