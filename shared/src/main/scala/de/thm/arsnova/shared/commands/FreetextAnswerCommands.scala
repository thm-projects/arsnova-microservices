package de.thm.arsnova.shared.commands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.FreetextAnswer

object FreetextAnswerCommands {
  sealed trait FreetextAnswerCommand[R]

  case class GetFreetextAnswer(id: UUID) extends FreetextAnswerCommand[FreetextAnswer]

  case class GetFreetextAnswersByQuestionId(id: UUID) extends FreetextAnswerCommand[Seq[FreetextAnswer]]

  case class CreateFreetextAnswer(answer: FreetextAnswer) extends FreetextAnswerCommand[Done]
}
