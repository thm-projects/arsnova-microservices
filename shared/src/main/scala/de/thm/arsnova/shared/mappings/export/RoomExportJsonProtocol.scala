package de.thm.arsnova.shared.mappings.export

import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.export.RoomExport
import de.thm.arsnova.shared.mappings.UUIDFormat

object RoomExportJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  import de.thm.arsnova.shared.mappings.export.ContentExportJsonProtocol._
  import de.thm.arsnova.shared.mappings.CommentJsonProtocol._
  import de.thm.arsnova.shared.mappings.ContentGroupJsonProtocol._

  implicit val roomExportFormat: RootJsonFormat[RoomExport] = jsonFormat5(RoomExport.apply)
}
