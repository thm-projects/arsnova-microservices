package de.thm.arsnova.contentservice.repositories

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery
import scala.concurrent.Future

import de.thm.arsnova.contentservice.repositories.definitions.ChoiceAnswersTable
import de.thm.arsnova.shared.entities.ChoiceAnswer

object ChoiceAnswerRepository {
  val db: Database = Database.forConfig("database")
  val choiceAnswersTable = TableQuery[ChoiceAnswersTable]

  def findById(choiceAnswerId: UUID): Future[ChoiceAnswer] = {
    db.run(choiceAnswersTable.filter(_.id === choiceAnswerId).result.head)
  }

  def findByQuestionId(contentId: UUID): Future[Seq[ChoiceAnswer]] = {
    db.run(choiceAnswersTable.filter(_.contentId === contentId).result)
  }

  def create(choiceAnswer: ChoiceAnswer): Future[Int] = {
    val itemWithId = choiceAnswer.copy(id = Some(UUID.randomUUID))
    db.run(choiceAnswersTable += itemWithId)
  }

  def update(choiceAnswer: ChoiceAnswer): Future[Int] = {
    db.run(choiceAnswersTable.filter(_.id === choiceAnswer.id.get)
      .map(a => a.answerOptionId).update(choiceAnswer.answerOptionId)
    )
  }

  def delete(choiceAnswerId: UUID): Future[Int] = {
    db.run(choiceAnswersTable.filter(_.id === choiceAnswerId).delete)
  }

  def deleteAllByContentId(contentId: UUID): Future[Int] = {
    db.run(choiceAnswersTable.filter(_.contentId === contentId).delete)
  }
}