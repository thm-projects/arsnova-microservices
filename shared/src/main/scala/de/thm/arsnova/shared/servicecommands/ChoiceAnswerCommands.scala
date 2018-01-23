package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import akka.actor.ActorRef
import de.thm.arsnova.shared.entities.ChoiceAnswer
import de.thm.arsnova.shared.entities.export.AnswerOptionExport

object ChoiceAnswerCommands {
  sealed trait ChoiceAnswerCommand extends ServiceCommand {
    def contentId: UUID
  }

  case class ChoiceAnswerCommandWithRole(cmd: ChoiceAnswerCommand, role: String, ret: ActorRef)

  case class GetChoiceAnswers(contentId: UUID) extends ChoiceAnswerCommand

  case class GetChoiceAnswer(contentId: UUID, id: UUID) extends ChoiceAnswerCommand

  case class CreateChoiceAnswer(contentId: UUID, roomId: UUID, answer: ChoiceAnswer, userId: UUID) extends ChoiceAnswerCommand

  case class DeleteChoiceAnswer(contentId: UUID, roomId: UUID, id: UUID, userId: UUID) extends ChoiceAnswerCommand

  case class GetChoiceStatistics(contentId: UUID) extends ChoiceAnswerCommand

  case class ImportChoiceAnswers(contentId: UUID, roomId: UUID, exportedAnswerOptions: Seq[AnswerOptionExport]) extends ChoiceAnswerCommand

  case class GetTransitions(contentId: UUID, startRound: Int, endRound: Int) extends ChoiceAnswerCommand
}
