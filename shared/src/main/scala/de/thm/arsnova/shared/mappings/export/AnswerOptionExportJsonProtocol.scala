package de.thm.arsnova.shared.mappings.export

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.export.AnswerOptionExport

object AnswerOptionExportJsonProtocol extends DefaultJsonProtocol {
  implicit val answerOptionExportFormat: RootJsonFormat[AnswerOptionExport] = jsonFormat4(AnswerOptionExport.apply)
}
