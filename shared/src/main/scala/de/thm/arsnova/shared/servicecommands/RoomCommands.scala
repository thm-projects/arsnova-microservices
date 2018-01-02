package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import akka.actor.ActorRef
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.{Room, ContentGroup}

object RoomCommands {
  import de.thm.arsnova.shared.mappings.RoomJsonProtocol._

  sealed trait RoomCommand extends ServiceCommand {
    def id: UUID
  }

  case class RoomCommandWithRole(cmd: RoomCommand, role: String, ret: ActorRef)

  case class GetRoom(id: UUID) extends RoomCommand

  object GetRoomFormat extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[GetRoom] = jsonFormat1(GetRoom)
  }

  case class CreateRoom(id: UUID, room: Room, userId: UUID) extends RoomCommand

  object CreateRoomFormat extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[CreateRoom] = jsonFormat3(CreateRoom)
  }

  case class UpdateRoom(id: UUID, room: Room, userId: UUID) extends RoomCommand

  case class DeleteRoom(id: UUID, userId: UUID) extends RoomCommand

  case class GetContentListByRoomId(id: UUID, group: Option[String]) extends RoomCommand

  case class AutoSortContentGroup(id: UUID, group: String) extends RoomCommand

  case class ExportRoom(id: UUID, userId: UUID) extends RoomCommand

  case class UpdateContentGroups(groups: Map[String, ContentGroup])
}
