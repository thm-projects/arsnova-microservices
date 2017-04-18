package de.thm.arsnova.commentservice

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.sql.SqlProfile.ColumnOption._

import de.thm.arsnova.shared.entities.Comment

class CommentsTable(tag: Tag) extends Table[Comment](tag, "comments"){
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey, O.AutoInc)
  def userId: Rep[UUID] = column[UUID]("user_id")
  def sessionId: Rep[UUID] = column[UUID]("session_id")
  def isRead: Rep[Boolean] = column[Boolean]("is_read")
  def subject: Rep[String] = column[String]("subject")
  def text: Rep[String] = column[String]("content")
  def createdAt: Rep[String] = column[String]("created_at")

  def * = (id.?, userId, sessionId, isRead, subject, text, createdAt) <> ((Comment.apply _).tupled, Comment.unapply)
}
