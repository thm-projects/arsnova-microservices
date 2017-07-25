package de.thm.arsnova.authservice.repositories.definitions

import java.util.UUID
import slick.driver.PostgresDriver.api._

import de.thm.arsnova.shared.entities.SessionRole

class SessionRolesTables(tag: Tag) extends Table[SessionRole](tag, "session_roles") {
  def userId: Rep[UUID] = column[UUID]("user_id")
  def sessionId: Rep[UUID] = column[UUID]("session_id")
  def role: Rep[String] = column[String]("role")

  def * = (userId, sessionId, role) <> ((SessionRole.apply _).tupled, SessionRole.unapply)
}

