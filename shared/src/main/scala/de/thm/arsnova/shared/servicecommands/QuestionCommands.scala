package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.Question

object QuestionCommands {
  sealed trait QuestionCommand extends ServiceCommand {
    def sessionid: UUID
  }

  case class GetQuestion(sessionid: UUID, id: UUID) extends QuestionCommand

  case class GetQuestionsBySessionId(sessionid: UUID) extends QuestionCommand

  case class GetQuestionsBySessionIdAndVariant(sessionid: UUID, variant: String) extends QuestionCommand

  case class CreateQuestion(sessionid: UUID, question: Question) extends QuestionCommand
}
