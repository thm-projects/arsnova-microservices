package de.thm.arsnova.authservice.repositories

import java.util.UUID

import scala.util.Try
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery
import de.thm.arsnova.shared.entities.DbUser

object DbUserRepository extends BaseRepository {
  import de.thm.arsnova.authservice.Context._

  def findById(userId: UUID): Future[Option[DbUser]] = {
    db.run(dbUsersTable.filter(_.id === userId).result.headOption)
  }

  def create(user: DbUser): Future[Try[Int]] = {
    db.run((dbUsersTable += user).asTry)
  }

  def update(newUser: DbUser, userId: UUID): Future[Int] = {
    db.run(dbUsersTable.filter(_.id === userId)
      .map(user => (user.username, user.password))
      .update((newUser.username, newUser.password))
    )
  }

  def delete(userId: UUID): Future[Int] = {
    db.run(dbUsersTable.filter(_.id === userId).delete)
  }

  def getUserByTokenString(tokenString: String): Future[Option[DbUser]] = {
    val qry = for {
      token <- tokensTable filter(_.token === tokenString)
      user <- dbUsersTable if (token.userId === user.id)
    } yield (user)
    db.run(qry.result.headOption)
  }

  def verifyLogin(username: String, password: String): Future[Option[UUID]] = {
    db.run(dbUsersTable.filter(u => (u.username === username && u.password === password)).map(_.id).result.headOption)
  }
}
