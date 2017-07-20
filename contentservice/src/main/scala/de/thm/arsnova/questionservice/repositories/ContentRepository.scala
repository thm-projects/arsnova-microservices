package de.thm.arsnova.questionservice.repositories

import java.util.UUID
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import definitions.ContentListTable
import de.thm.arsnova.shared.entities.Content

object ContentRepository {
  import de.thm.arsnova.questionservice.Context._

  val db: Database = Database.forConfig("database")
  val questionsTable = TableQuery[ContentListTable]

  def findById(contentId: UUID): Future[Option[Content]] = {
    val contentFuture: Future[Option[Content]] = db.run(questionsTable.filter(_.id === contentId).result.headOption)

    contentFuture.map {
      case Some(c) =>
        c.format match {
          case "mc" => Await.result(AnswerOptionRepository.findByQuestionId(c.id.get).map(a => Some(c.copy(answerOptions = Some(a)))), 5.seconds)
          case _ => Some(c)
        }
      case None => None
    }
  }

  def findBySessionId(sessionId: UUID): Future[Seq[Content]] = {
    val contentSeqFuture: Future[Seq[Content]] = db.run(questionsTable.filter(_.sessionId === sessionId).result)
    contentSeqFuture.map((cSequence: Seq[Content]) =>
      cSequence.map((c: Content) =>
        c.format match {
          case "mc" => Await.result(AnswerOptionRepository.findByQuestionId(c.id.get).map(a => c.copy(answerOptions = Some(a))), 5.seconds)
          case _ => c
        }
      )
    )
  }

  def create(newContent: Content): Future[Content] = {
    db.run(questionsTable += newContent).map { w =>
      newContent.answerOptions match {
        case Some(aO) => AnswerOptionRepository.create(aO)
        case None =>
      }
      newContent
    }
  }
}
