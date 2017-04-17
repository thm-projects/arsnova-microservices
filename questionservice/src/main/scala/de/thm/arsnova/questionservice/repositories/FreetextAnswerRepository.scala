package de.thm.arsnova.questionservice.repositories

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery
import scala.concurrent.Future

import de.thm.arsnova.questionservice.repositories.definitions.FreetextAnswersTable
import de.thm.arsnova.shared.entities.FreetextAnswer

object FreetextAnswerRepository {
  val db: Database = Database.forConfig("database")
  val freetextAnswersTable = TableQuery[FreetextAnswersTable]

  def findById(freetextAnswerId: UUID): Future[FreetextAnswer] = {
    db.run(freetextAnswersTable.filter(_.id === freetextAnswerId).result.head)
  }

  def findByQuestionId(questionId: UUID): Future[Seq[FreetextAnswer]] = {
    db.run(freetextAnswersTable.filter(_.questionId === questionId).result)
  }

  def create(freetextAnswer: FreetextAnswer): Future[Int] = {
    db.run(freetextAnswersTable += freetextAnswer)
  }

  def update(freetextAnswer: FreetextAnswer): Future[Int] = {
    db.run(freetextAnswersTable.filter(_.id === freetextAnswer.id.get)
      .map(a => (a.subject, a.content)).update(freetextAnswer.subject, freetextAnswer.text)
    )
  }

  def delete(freetextAnswerId: UUID): Future[Int] = {
    db.run(freetextAnswersTable.filter(_.id === freetextAnswerId).delete)
  }

  def deleteAllByQuestionId(questionId: UUID): Future[Int] = {
    db.run(freetextAnswersTable.filter(_.questionId === questionId).delete)
  }
}