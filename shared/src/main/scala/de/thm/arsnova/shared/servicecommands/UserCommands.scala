package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import de.thm.arsnova.shared.entities.User

object UserCommands {
  trait UserCommand extends ServiceCommand {
    def userId: UUID
  }

  case class GetUser(userId: UUID) extends UserCommand

  case class CreateUser(userId: UUID, user: User) extends UserCommand

  case class MakeUserOwner(userId: UUID, roomId: UUID) extends UserCommand

  case class GetRoleForRoom(userId: UUID, roomId: UUID) extends UserCommand

  case class GetUserRooms(userId: UUID, withRole: Option[String] = None) extends UserCommand
}
