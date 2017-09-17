package de.thm.arsnova.authservice.repositories

import java.util.UUID
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import de.thm.arsnova.authservice.repositories.definitions._

trait BaseRepository {
  val db: Database = Database.forConfig("database")
  val dbUsersTable = TableQuery[DbUsersTable]
  val tokensTable = TableQuery[TokensTable]
}
