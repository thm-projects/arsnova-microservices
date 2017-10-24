package de.thm.arsnova.shared.mappings.export

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.export.FreetextAnswerExport

object FreetextAnswerExportJsonProtocol extends DefaultJsonProtocol {
  implicit val freetextAnswerExportFormat: RootJsonFormat[FreetextAnswerExport] = jsonFormat2(FreetextAnswerExport.apply)
}

