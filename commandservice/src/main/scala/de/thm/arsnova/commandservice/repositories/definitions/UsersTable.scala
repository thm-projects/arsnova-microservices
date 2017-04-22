package de.thm.arsnova.commandservice.repositories.definitions

import java.util.UUID
import slick.driver.PostgresDriver.api._

import de.thm.arsnova.shared.entities.User

class UsersTable(tag: Tag) extends Table[User](tag, "users"){
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def username: Rep[String] = column[String]("username")
  def password: Rep[String] = column[String]("pwd")

  def * = (id.?, username, password) <> ((User.apply _).tupled, User.unapply)
}

