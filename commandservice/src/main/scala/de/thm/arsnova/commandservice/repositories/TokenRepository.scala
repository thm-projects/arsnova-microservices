package de.thm.arsnova.commandservice.repositories

import java.util.UUID
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import de.thm.arsnova.shared.entities.Token

object TokenRepository {
  def create(userId: UUID): Future[Token] = {
    val now = new Date().getTime
    val token = Token(randomUUID.toString, userId, now.toString, None, now.toString)
    tokensTable.returning(tokensTable) += token
  }
}