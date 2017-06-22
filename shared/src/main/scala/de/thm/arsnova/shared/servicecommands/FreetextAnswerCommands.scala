package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import de.thm.arsnova.shared.entities.FreetextAnswer

object FreetextAnswerCommands {
  sealed trait FreetextAnswerCommand extends ServiceCommand

  case class GetFreetextAnswer(id: UUID) extends FreetextAnswerCommand

  case class GetFreetextAnswersByQuestionId(id: UUID) extends FreetextAnswerCommand

  case class CreateFreetextAnswer(answer: FreetextAnswer) extends FreetextAnswerCommand
}