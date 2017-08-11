package de.thm.arsnova.authservice.repositories

import java.util.UUID
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._

import de.thm.arsnova.shared.entities.SessionRole

object SessionRoleRepository extends BaseRepository {
  import de.thm.arsnova.authservice.Context._

  def addSessionRole(sessionRole: SessionRole): Future[SessionRole] =
    db.run(sessionRolesTables += sessionRole).map(_ => sessionRole)

  def getSessionRole(userId: UUID, sessionId: UUID): Future[Option[SessionRole]] = {
    val qry = sessionRolesTables.filter(e => (e.userId === userId) && (e.sessionId === sessionId))
    db.run(qry.result.headOption)
  }

  def getAllSessionRoles(userId: UUID): Future[Seq[SessionRole]] = {
    val qry = sessionRolesTables.filter(_.userId === userId)
    db.run(qry.result)
  }

  def getAllSessionsByRole(userId: UUID, role: String): Future[Seq[SessionRole]] = {
    val qry = sessionRolesTables.filter(e => e.userId === userId && e.role === role)
    db.run(qry.result)
  }

  def deleteSessionRole(role: SessionRole): Future[Int] = {
    val qry = sessionRolesTables.filter(r => r.sessionId === role.sessionId && r.userId === role.userId).delete
    db.run(qry)
  }
}
