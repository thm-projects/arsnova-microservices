package de.thm.arsnova.commandservice.repositories

import java.util.UUID
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import de.thm.arsnova.shared.entities.User

object UserRepository {
  import Context._

  def findById(userId: UUID): Future[User] = {
    db.run(usersTable.filter(_.id === userId).result.head)
  }

  def create(user: User): Future[UUID] = {
    val uId = UUID.randomUUID
    val itemWithId = user.copy(id = Some(uId))
    db.run(usersTable += itemWithId)
  }

  def update(newUser: User, userId: UUID): Future[Int] = {
    db.run(usersTable.filter(_.id === userId)
      .map(user => (user.username, user.password))
      .update((newUser.userName, newUser.password))
    )
  }

  def delete(userId: UUID): Future[Int] = {
    db.run(usersTable.filter(_.id === userId).delete)
  }

  def getByLoginTokenString(loginTokenString: String): Future[User] = {
    val qry = for {
      token <- tokensTable filter(_.token === loginTokenString)
      user <- usersTable if (token.userId === user.id)
    } yield (user)
    db.run(qry.result.head)
  }

  def verifyLogin(username: String, password: String) = Future[Option[UUID]] = {
    db.run(usersTable.filter(_.username === username && _.password === password).map(_.id).result.headOption)
  }
}
