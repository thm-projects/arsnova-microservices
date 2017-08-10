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

  def findById(id: UUID): Future[Option[Comment]] = {
    val qry = commentsTable.filter(_.id === id).result.headOption
    db.run(qry)
  }

  def findBySessionId(sessionId: UUID): Future[Seq[Comment]] = {
    val qry = commentsTable.filter(_.sessionId === sessionId).result
    db.run(qry)
  }

  def create(comment: Comment): Future[Comment] = {
    val qry = commentsTable += comment
    db.run(qry).map(_ => comment)
  }

  def delete(id: UUID): Future[Int] = {
    val qry = commentsTable.filter(_.id === id).delete
    db.run(qry)
  }

  def deleteAllSessionContent(sessionId: UUID): Future[Int] = {
    val qry = commentsTable.filter(_.sessionId === sessionId).delete
    db.run(qry)
  }

  def markAsRead(ids: Seq[UUID]): Future[Seq[Int]] = {
    val qrys = ids.map { id =>
      commentsTable.filter(_.id === id).map(_.isRead).update(true)
    }
    db.run(DBIO.sequence(qrys))
  }
}