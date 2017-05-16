package de.thm.arsnova.shared.servicecommands

import java.util.UUID
import akka.Done

import de.thm.arsnova.shared.entities.Comment

object CommentCommands {
  sealed trait CommentCommand extends ServiceCommand

  case class GetComment(id: UUID) extends CommentCommand

  case class GetCommentBySessionId(id: UUID) extends CommentCommand

  case class CreateComment(comment: Comment) extends CommentCommand
}
