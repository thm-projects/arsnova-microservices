package de.thm.arsnova.authservice.repositories

import java.util.UUID
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._

import de.thm.arsnova.shared.entities.SessionRole

object SessionRoleRepository extends BaseRepository {
  import de.thm.arsnova.authservice.Context._

  def addSessionRole(sessionRole: SessionRole): Future[SessionRole] =
    db.run(sessionRolesTables += sessionRole).map(_ => sessionRole)

  def getSessionRole(userId: UUID, sessionId: UUID): Future[SessionRole] = {
    val qry = sessionRolesTables.filter(e => (e.userId === userId) && (e.sessionId === sessionId))
    db.run(qry.result)
  }
}
