package de.thm.arsnova.shared.servicecommands

import java.util.UUID
import akka.Done

import de.thm.arsnova.shared.entities.Comment

object CommentCommands {
  sealed trait CommentCommand extends ServiceCommand {
    def sessionId: UUID
  }

  case class GetComment(sessionId: UUID, id: UUID) extends CommentCommand

  case class GetCommentsBySessionId(sessionId: UUID) extends CommentCommand

  case class CreateComment(sessionId: UUID, comment: Comment) extends CommentCommand
}
