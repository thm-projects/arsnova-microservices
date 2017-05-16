package de.thm.arsnova.authservice.repositories.definitions

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.sql.SqlProfile.ColumnOption._

import de.thm.arsnova.shared.entities.Token

class TokensTable(tag: Tag) extends Table[Token](tag, "tokens") {
  def token: Rep[String] = column[String]("token", O.PrimaryKey)
  def userId: Rep[UUID] = column[UUID]("user_id")
  def created: Rep[String] = column[String]("created")
  def modified: Rep[String] = column[String]("modified")
  def lastUsed: Rep[String] = column[String]("last_used")

  def * = (token, userId, created, modified.?, lastUsed) <> ((Token.apply _).tupled, Token.unapply)
}