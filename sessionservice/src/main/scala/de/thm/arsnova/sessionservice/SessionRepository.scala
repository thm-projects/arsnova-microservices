package de.thm.arsnova.sessionservice

import java.util.UUID
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import de.thm.arsnova.shared.entities.Session

/**
  * Created by tekay on 4/15/17.
  */
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

  def create(session: Session): Future[Int] = {
    val sessionWithId = session.copy(id = Some(UUID.randomUUID))
    db.run(sessionsTable += sessionWithId)
  }
}