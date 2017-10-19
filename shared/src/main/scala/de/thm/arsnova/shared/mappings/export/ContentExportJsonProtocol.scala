package de.thm.arsnova.shared.mappings.export

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.export.ContentExport
import de.thm.arsnova.shared.mappings.UUIDFormat

object ContentExportJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  import AnswerOptionExportJsonProtocol._
  import FreetextAnswerExportJsonProtocol._
  import de.thm.arsnova.shared.mappings.ContentJsonProtocol.FormatAttributesJsonProtocol

  implicit val contentExportExportFormat: RootJsonFormat[ContentExport] = jsonFormat15(ContentExport)
}


