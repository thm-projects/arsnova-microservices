package de.thm.arsnova.questionservice.repositories

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery
import scala.concurrent.Future

import de.thm.arsnova.questionservice.repositories.definitions.ChoiceAnswersTable
import de.thm.arsnova.shared.entities.ChoiceAnswer

object ChoiceAnswerRepository {
  val db: Database = Database.forConfig("database")
  val choiceAnswersTable = TableQuery[ChoiceAnswersTable]

  def findById(choiceAnswerId: UUID): Future[ChoiceAnswer] = {
    db.run(choiceAnswersTable.filter(_.id === choiceAnswerId).result.head)
  }

  def findByQuestionId(questionId: UUID): Future[Seq[ChoiceAnswer]] = {
    db.run(choiceAnswersTable.filter(_.questionId === questionId).result)
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

  def deleteAllByQuestionId(questionId: UUID): Future[Int] = {
    db.run(choiceAnswersTable.filter(_.questionId === questionId).delete)
  }
}