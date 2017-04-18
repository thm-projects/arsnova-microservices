package de.thm.arsnova.commentservice

import java.util.UUID
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import de.thm.arsnova.shared.entities.Comment

object CommentRepository {
  import Context._

  val db: Database = Database.forConfig("database")
  val commentsTable = TableQuery[CommentsTable]

  def findById(id: UUID): Future[Comment] = {
    db.run(commentsTable.filter(_.id === id).result.head)
  }

  def findBySessionId(sessionId: UUID): Future[Seq[Comment]] = {
    db.run(commentsTable.filter(_.sessionId === sessionId).result)
  }

  def create(comment: Comment): Future[UUID] = {
    val cId = UUID.randomUUID
    val itemWithId = comment.copy(id = Some(cId))
    db.run(commentsTable += itemWithId).map(_ => cId)
  }
}