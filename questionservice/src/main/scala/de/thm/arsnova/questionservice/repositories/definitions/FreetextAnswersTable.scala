package de.thm.arsnova.questionservice.repositories.definitions

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.lifted.ForeignKeyQuery

import de.thm.arsnova.shared.entities.{FreetextAnswer, Question}

class FreetextAnswersTable(tag: Tag) extends Table[FreetextAnswer](tag, "freetext_answers") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def questionId: Rep[UUID] = column[UUID]("question_id")
  def sessionId: Rep[UUID] = column[UUID]("session_id")
  def subject: Rep[String] = column[String]("subject")
  def content: Rep[String] = column[String]("content")

  def * = (id.?, questionId, sessionId, subject, content) <> ((FreetextAnswer.apply _).tupled, FreetextAnswer.unapply)

  def question: ForeignKeyQuery[QuestionsTable, Question] = foreignKey("freetext_answer_question_fk", questionId, TableQuery[QuestionsTable])(_.id)
}
