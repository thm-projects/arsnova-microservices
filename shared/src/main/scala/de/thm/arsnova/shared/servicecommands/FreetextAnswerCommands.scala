package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import akka.actor.ActorRef
import de.thm.arsnova.shared.entities.{FreetextAnswer, Content}
import de.thm.arsnova.shared.entities.export.FreetextAnswerExport

object FreetextAnswerCommands {
  sealed trait FreetextAnswerCommand extends ServiceCommand {
    def contentId: UUID
  }

  case class FreetextAnswerCommandWithRole(cmd: FreetextAnswerCommand, role: String, ret: ActorRef)

  case class GetFreetextAnswers(contentId: UUID) extends FreetextAnswerCommand

  case class GetFreetextAnswer(contentId: UUID, id: UUID) extends FreetextAnswerCommand

  case class CreateFreetextAnswer(contentId: UUID, roomId: UUID, answer: FreetextAnswer, userId: UUID) extends FreetextAnswerCommand

  case class DeleteFreetextAnswer(contentId: UUID, roomId: UUID, id: UUID, userId: UUID) extends FreetextAnswerCommand

  case class GetFreetextStatistics(contentId: UUID) extends FreetextAnswerCommand

  case class ImportFreetextAnswers(contentId: UUID, roomId: UUID, content: Content, exportedAnswers: Seq[FreetextAnswerExport]) extends FreetextAnswerCommand
}
