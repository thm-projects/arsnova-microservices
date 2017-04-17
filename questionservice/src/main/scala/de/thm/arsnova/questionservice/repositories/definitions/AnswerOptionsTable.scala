package de.thm.arsnova.questionservice.repositories.definitions

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.lifted.ForeignKeyQuery
import spray.json._

import de.thm.arsnova.shared.entities.{AnswerOption, Question}

class AnswerOptionsTable(tag: Tag) extends Table[AnswerOption](tag, "answer_options") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def questionId: Rep[UUID] = column[UUID]("question_id")
  def correct: Rep[Boolean] = column[Boolean]("correct")
  def text: Rep[String] = column[String]("content")
  def value: Rep[Int] = column[Int]("points")

  def * = (id.?, questionId.?, correct, text, value) <> ((AnswerOption.apply _).tupled, AnswerOption.unapply)

  def question: ForeignKeyQuery[QuestionsTable, Question] = foreignKey("answer_option_question_fk", questionId, TableQuery[QuestionsTable])(_.id)
}
