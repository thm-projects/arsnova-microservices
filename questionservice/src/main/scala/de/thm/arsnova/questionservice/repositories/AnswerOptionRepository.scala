package de.thm.arsnova.questionservice.repositories

import java.util.UUID
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import definitions.AnswerOptionsTable
import de.thm.arsnova.shared.entities.AnswerOption

object AnswerOptionRepository {
  val db: Database = Database.forConfig("database")
  val answerOptionsTable = TableQuery[AnswerOptionsTable]

  def findById(id: UUID): Future[AnswerOption] = {
    db.run(answerOptionsTable.filter(_.id === id).result.head)
  }

  def findByQuestionId(id: UUID): Future[Seq[AnswerOption]] = {
    db.run(answerOptionsTable.filter(_.questionId === id).result)
  }

  def create(answerOptions: Seq[AnswerOption]): Seq[Future[Int]] = {
    val itemsWithId: Seq[AnswerOption] = answerOptions.map(_.copy(id = Some(UUID.randomUUID)))
    /*val wat = answerOptionsTable ++= itemsWithId
    db.run(wat)*/
    itemsWithId.map(item => db.run(answerOptionsTable += item))
  }
}