package de.thm.arsnova.authservice.repositories

import java.util.UUID
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import de.thm.arsnova.shared.entities.User

object UserRepository extends BaseRepository {
  import de.thm.arsnova.authservice.Context._

  def findById(userId: UUID): Future[Option[User]] = {
    db.run(usersTable.filter(_.id === userId).result.headOption)
  }

  def create(user: User): Future[User] = {
    db.run(usersTable += user).map(_ => user)
  }

  def update(newUser: User, userId: UUID): Future[Int] = {
    db.run(usersTable.filter(_.id === userId)
      .map(user => (user.username, user.password))
      .update((newUser.username, newUser.password))
    )
  }

  def delete(userId: UUID): Future[Int] = {
    db.run(usersTable.filter(_.id === userId).delete)
  }

  def getUserByTokenString(tokenString: String): Future[Option[User]] = {
    val qry = for {
      token <- tokensTable filter(_.token === tokenString)
      user <- usersTable if (token.userId === user.id)
    } yield (user)
    db.run(qry.result.headOption)
  }

  def verifyLogin(username: String, password: String): Future[Option[UUID]] = {
    db.run(usersTable.filter(u => (u.username === username && u.password === password)).map(_.id).result.headOption)
  }
}
