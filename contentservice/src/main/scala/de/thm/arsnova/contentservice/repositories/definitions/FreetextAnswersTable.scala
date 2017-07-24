package de.thm.arsnova.contentservice.repositories.definitions

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.lifted.ForeignKeyQuery

import de.thm.arsnova.shared.entities.{FreetextAnswer, Content}

class FreetextAnswersTable(tag: Tag) extends Table[FreetextAnswer](tag, "freetext_answers") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def contentId: Rep[UUID] = column[UUID]("content_id")
  def sessionId: Rep[UUID] = column[UUID]("session_id")
  def subject: Rep[String] = column[String]("subject")
  def content: Rep[String] = column[String]("content")

  def * = (id.?, contentId, sessionId, subject, content) <> ((FreetextAnswer.apply _).tupled, FreetextAnswer.unapply)

  def question: ForeignKeyQuery[ContentListTable, Content] = foreignKey("freetext_answer_question_fk", contentId, TableQuery[ContentListTable])(_.id)
}
