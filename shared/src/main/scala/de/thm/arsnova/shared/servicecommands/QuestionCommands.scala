package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.Question

object QuestionCommands {
  sealed trait QuestionCommand extends ServiceCommand

  case class GetQuestion(id: UUID) extends QuestionCommand

  case class GetQuestionsBySessionId(id: UUID) extends QuestionCommand

  case class GetQuestionsBySessionIdAndVariant(id: UUID, variant: String) extends QuestionCommand

  case class CreateQuestion(question: Question) extends QuestionCommand
}
