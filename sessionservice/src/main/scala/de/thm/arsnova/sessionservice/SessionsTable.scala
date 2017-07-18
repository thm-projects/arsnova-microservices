package de.thm.arsnova.sessionservice

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.sql.SqlProfile.ColumnOption._

import de.thm.arsnova.shared.entities.Session

class SessionsTable(tag: Tag) extends Table[Session](tag, "sessions"){
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def key: Rep[String] = column[String]("keyword")
  def userId: Rep[UUID] = column[UUID]("user_id")
  def title: Rep[String] = column[String]("title")
  def shortName: Rep[String] = column[String]("short_name")
  def lastOwnerActivity: Rep[String] = column[String]("last_owner_activity")
  def creationTime: Rep[String] = column[String]("creation_time")
  def active: Rep[Boolean] = column[Boolean]("active")
  def feedbackLock: Rep[Boolean] = column[Boolean]("feedback_lock")
  def flipFlashcards: Rep[Boolean] = column[Boolean]("flip_flashcards")

  def * = (id.?, key.?, userId, title, shortName, lastOwnerActivity, creationTime, active, feedbackLock, flipFlashcards) <> (Session.tupled, Session.unapply)
}
