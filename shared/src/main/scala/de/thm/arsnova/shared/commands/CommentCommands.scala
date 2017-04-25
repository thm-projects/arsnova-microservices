package de.thm.arsnova.shared.commands

import java.util.UUID
import akka.Done

import de.thm.arsnova.shared.entities.Comment

object CommentCommands {
  sealed trait CommentCommand[R] extends Command[R]

  case class GetComment(id: UUID) extends CommentCommand[Comment]

  case class GetCommentBySessionId(id: UUID) extends CommentCommand[Seq[Comment]]

  case class CreateComment(comment: Comment) extends CommentCommand[Done]
}
