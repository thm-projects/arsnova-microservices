package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import akka.actor.ActorRef
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.Room

object RoomCommands {
  import de.thm.arsnova.shared.mappings.RoomJsonProtocol._

  sealed trait RoomCommand extends ServiceCommand {
    def id: UUID
  }

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
}
