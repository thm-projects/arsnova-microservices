package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import akka.actor.ActorRef
import de.thm.arsnova.shared.entities.export.RoomExport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.{ContentGroup, Room}

object RoomCommands {

  sealed trait RoomCommand extends ServiceCommand {
    def id: UUID
  }

  case class RoomCommandWithRole(cmd: RoomCommand, role: String, ret: ActorRef)

  case class GetRoom(id: UUID) extends RoomCommand

  case class CreateRoom(id: UUID, room: Room, userId: UUID) extends RoomCommand

  case class UpdateRoom(id: UUID, room: Room, userId: UUID) extends RoomCommand

  case class DeleteRoom(id: UUID, userId: UUID) extends RoomCommand

  case class GetContentListByRoomId(id: UUID, group: Option[String]) extends RoomCommand

  case class AutoSortContentGroup(id: UUID, group: String) extends RoomCommand

  case class ExportRoom(id: UUID, userId: UUID) extends RoomCommand

  case class ImportRoom(id: UUID, keyword: String, userId: UUID, exportedRoom: RoomExport) extends RoomCommand

  case class UpdateContentGroups(groups: Map[String, ContentGroup])
}
