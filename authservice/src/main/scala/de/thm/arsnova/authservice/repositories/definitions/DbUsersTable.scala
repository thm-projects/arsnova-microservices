package de.thm.arsnova.authservice.repositories.definitions

import java.util.UUID
import slick.driver.PostgresDriver.api._

import de.thm.arsnova.shared.entities.DbUser

class DbUsersTable(tag: Tag) extends Table[DbUser](tag, "users"){
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def username: Rep[String] = column[String]("username")
  def password: Rep[String] = column[String]("pwd")

  def * = (id.?, username, password) <> ((DbUser.apply _).tupled, DbUser.unapply)
}

