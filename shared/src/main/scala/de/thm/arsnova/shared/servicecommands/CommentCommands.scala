package de.thm.arsnova.shared.servicecommands

import java.util.UUID
import akka.Done

import de.thm.arsnova.shared.entities.Comment

object CommentCommands {
  sealed trait CommentCommand extends ServiceCommand {
    def roomId: UUID
  }

  case class GetComment(roomId: UUID, id: UUID) extends CommentCommand

  case class GetCommentsByRoomId(roomId: UUID) extends CommentCommand

  case class GetUnreadComments(roomId: UUID) extends CommentCommand

  case class CreateComment(roomId: UUID, comment: Comment, userId: UUID) extends CommentCommand

  case class DeleteComment(roomId: UUID, commentId: UUID) extends CommentCommand
}
