package de.thm.arsnova.questionservice.repositories

import java.util.UUID
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import definitions.QuestionsTable
import de.thm.arsnova.shared.entities.Question

object QuestionRepository {
  import de.thm.arsnova.questionservice.Context._

  val db: Database = Database.forConfig("database")
  val questionsTable = TableQuery[QuestionsTable]

  def findById(questionId: UUID): Future[Question] = {
    val questionFuture: Future[Question] = db.run(questionsTable.filter(_.id === questionId).result.head)
    questionFuture.map((q: Question) =>
      q.format match {
        case "mc" => Await.result(AnswerOptionRepository.findByQuestionId(q.id.get).map(a => q.copy(answerOptions = Some(a))), 5.seconds)
        case _ => q
      }
    )
  }

  def findBySessionId(sessionId: UUID): Future[Seq[Question]] = {
    val questionSeqFuture: Future[Seq[Question]] = db.run(questionsTable.filter(_.sessionId === sessionId).result)
    questionSeqFuture.map((qSequence: Seq[Question]) =>
      qSequence.map((q: Question) =>
        q.format match {
          case "mc" => Await.result(AnswerOptionRepository.findByQuestionId(q.id.get).map(a => q.copy(answerOptions = Some(a))), 5.seconds)
          case _ => q
        }
      )
    )
  }

  def create(newQuestion: Question): Future[Int] = {
    newQuestion.format match {
      case "mc" => {
        (db.run(questionsTable returning questionsTable.map(_.id) += newQuestion)).map(
          qId => {
            val answerOptionsWithQId = newQuestion.answerOptions.get.map(_.copy(questionId = Some(qId)))
            AnswerOptionRepository.create(answerOptionsWithQId)
            1
          })
      }
      case _ => db.run(questionsTable += newQuestion)
    }
  }
}