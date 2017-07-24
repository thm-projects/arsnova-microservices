package de.thm.arsnova.contentservice.repositories

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery
import scala.concurrent.Future

import de.thm.arsnova.contentservice.repositories.definitions.FreetextAnswersTable
import de.thm.arsnova.shared.entities.FreetextAnswer

object FreetextAnswerRepository {
  val db: Database = Database.forConfig("database")
  val freetextAnswersTable = TableQuery[FreetextAnswersTable]

  def findById(freetextAnswerId: UUID): Future[FreetextAnswer] = {
    db.run(freetextAnswersTable.filter(_.id === freetextAnswerId).result.head)
  }

  def findByQuestionId(contentId: UUID): Future[Seq[FreetextAnswer]] = {
    db.run(freetextAnswersTable.filter(_.contentId === contentId).result)
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

  def deleteAllByQuestionId(contentId: UUID): Future[Int] = {
    db.run(freetextAnswersTable.filter(_.contentId === contentId).delete)
  }
}