package de.thm.arsnova.sessionservice

import java.util.UUID
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import de.thm.arsnova.shared.entities.{Session, User}
import de.thm.arsnova.shared.Exceptions._

object SessionRepository {
  import Context._

  val db: Database = Database.forConfig("database")
  val sessionsTable = TableQuery[SessionsTable]

  def findById(id: UUID): Future[Option[Session]] = {
    db.run(sessionsTable.filter(_.id === id).result.headOption)
  }

  def create(session: Session): Future[Session] = {
    db.run(sessionsTable += session).map(_ => session)
  }

  def getKeywordList(): Future[Seq[(String, UUID)]] = {
    val qry = sessionsTable.map(s => (s.key, s.id)).result
    db.run(qry)
  }

  def update(session: Session): Future[Session] = {
    val qry = sessionsTable.filter(_.id === session.id.get)
      .map(ns => (ns.title, ns.shortName, ns.active, ns.feedbackLock, ns.flipFlashcards))
      .update((session.title, session.shortName, session.active, session.feedbackLock, session.flipFlashcards))
    db.run(qry)
    Future { session }
  }

  def delete(id: UUID): Future[Int] = {
    val qry = sessionsTable.filter(_.id === id).delete
    db.run(qry)
  }
}
