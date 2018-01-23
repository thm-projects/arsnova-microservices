package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import akka.actor.ActorRef
import de.thm.arsnova.shared.entities.ChoiceAnswer
import de.thm.arsnova.shared.entities.export.AnswerOptionExport

object ChoiceAnswerCommands {
  sealed trait ChoiceAnswerCommand extends ServiceCommand {
    def roomId: UUID
    def contentId: UUID
  }

  case class ChoiceAnswerCommandWithRole(cmd: ChoiceAnswerCommand, role: String, ret: ActorRef)

  case class GetChoiceAnswers(roomId: UUID, contentId: UUID) extends ChoiceAnswerCommand

  case class GetChoiceAnswer(roomId: UUID, contentId: UUID, id: UUID) extends ChoiceAnswerCommand

  case class CreateChoiceAnswer(roomId: UUID, contentId: UUID, answer: ChoiceAnswer, userId: UUID) extends ChoiceAnswerCommand

  case class DeleteChoiceAnswer(roomId: UUID, contentId: UUID, id: UUID, userId: UUID) extends ChoiceAnswerCommand

  case class GetChoiceStatistics(roomId: UUID, contentId: UUID) extends ChoiceAnswerCommand

  case class ImportChoiceAnswers(roomId: UUID, contentId: UUID, exportedAnswerOptions: Seq[AnswerOptionExport]) extends ChoiceAnswerCommand
}
