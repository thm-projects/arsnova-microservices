package de.thm.arsnova.contentservice.repositories.definitions

import java.util.UUID
import slick.driver.PostgresDriver.api._

import de.thm.arsnova.shared.entities.ChoiceAnswer

class ChoiceAnswersTable(tag: Tag) extends Table[ChoiceAnswer](tag, "choice_answers") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def contentId: Rep[UUID] = column[UUID]("content_id")
  def sessionId: Rep[UUID] = column[UUID]("session_id")
  def answerOptionId: Rep[UUID] = column[UUID]("answer_option_id")

  def * = (id.?, contentId, sessionId, answerOptionId) <> ((ChoiceAnswer.apply _).tupled, ChoiceAnswer.unapply)
}