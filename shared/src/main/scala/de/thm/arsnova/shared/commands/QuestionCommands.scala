package de.thm.arsnova.shared.commands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.Question

object QuestionCommands {
  sealed trait QuestionCommand[R]

  case class GetQuestion(id: UUID) extends QuestionCommand[Question]

  case class GetQuestionsBySessionId(id: UUID) extends QuestionCommand[Seq[Question]]

  case class GetQuestionsBySessionIdAndVariant(id: UUID, variant: String) extends QuestionCommand[Seq[Question]]

  case class CreateQuestion(question: Question) extends QuestionCommand[Done]
}
