package de.thm.arsnova.contentservice.repositories.definitions

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.lifted.ForeignKeyQuery
import spray.json._

import de.thm.arsnova.shared.entities.{AnswerOption, Content}

class AnswerOptionsTable(tag: Tag) extends Table[AnswerOption](tag, "answer_options") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def contentId: Rep[UUID] = column[UUID]("content_id")
  def correct: Rep[Boolean] = column[Boolean]("correct")
  def text: Rep[String] = column[String]("content")
  def value: Rep[Int] = column[Int]("points")

  def * = (id.?, contentId.?, correct, text, value) <> ((AnswerOption.apply _).tupled, AnswerOption.unapply)

  def question: ForeignKeyQuery[ContentListTable, Content] = foreignKey("answer_option_question_fk", contentId, TableQuery[ContentListTable])(_.id)
}
