package de.thm.arsnova.sessionservice

import java.util.UUID
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import de.thm.arsnova.shared.entities.Session

object SessionRepository {
  import Context._

  val db: Database = Database.forConfig("database")
  val sessionsTable = TableQuery[SessionsTable]

  def findById(id: UUID): Future[Session] = {
    db.run(sessionsTable.filter(_.id === id).result.head)
  }

  def findByKeyword(key: String): Future[Session] = {
    db.run(sessionsTable.filter(_.key === key).result.head)
  }

  def create(session: Session): Future[UUID] = {
    val sId = UUID.randomUUID
    val itemWithId = session.copy(id = Some(sId))
    db.run(sessionsTable += itemWithId).map(_ => sId)
  }
}
