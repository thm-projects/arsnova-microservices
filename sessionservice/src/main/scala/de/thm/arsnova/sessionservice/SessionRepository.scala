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

  def findById(id: UUID): Future[Session] = {
    db.run(sessionsTable.filter(_.id === id).result.head)
  }

  def findByKeyword(key: String): Future[Session] = {
    db.run(sessionsTable.filter(_.key === key).result.head)
  }

  def create(session: Session, user: Option[User]): Future[Session] = {
    user match {
      case None => Future.failed(NoUserException("createSession"))
      case Some(user) => {
        db.run(sessionsTable += session).map(_ => session)
      }
    }
  }

  def getKeywordList(): Future[Seq[(String, UUID)]] = {
    val qry = sessionsTable.map(s => (s.key, s.id)).result
    db.run(qry)
  }
}
