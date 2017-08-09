package de.thm.arsnova.contentservice.repositories

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
    db.run(answerOptionsTable.filter(_.contentId === id).result)
  }

  def create(answerOptions: Seq[AnswerOption]): Seq[Future[Int]] = {
    answerOptions.map { item =>
      val itemWithId = item.copy(id = Some(UUID.randomUUID))
      db.run(answerOptionsTable += itemWithId)
    }
  }

  def deleteByContentId(contentId: UUID): Future[Int] = {
    val qry = answerOptionsTable.filter(_.contentId === contentId).delete
    db.run(qry)
  }
}