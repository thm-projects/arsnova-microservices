package de.thm.arsnova.shared.mappings

import spray.json._
import de.thm.arsnova.shared.Exceptions.NoSuchSession

object NoSuchSessionJsonProtocol extends DefaultJsonProtocol {
  implicit object NoSuchSessionFormat extends RootJsonFormat[NoSuchSession] {
    def errorMessage(reason: String): String =
      s"Could not get session. So such $reason"

    override def write(obj: NoSuchSession): JsValue = {
      val parsedReason = obj.reason match {
        case Left(id) => errorMessage(s"id: $id")
        case Right(keyword) => errorMessage(s"keyword: $keyword")
      }
      JsString(parsedReason)
    }

    // don't really need a reader
    override def read(json: JsValue): NoSuchSession = {
      NoSuchSession(Right(""))
    }
  }
}
