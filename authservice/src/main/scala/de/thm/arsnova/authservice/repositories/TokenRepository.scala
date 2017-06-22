package de.thm.arsnova.authservice.repositories

import java.util.{UUID, Date}
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import de.thm.arsnova.shared.entities.Token

object TokenRepository extends BaseRepository {
  import de.thm.arsnova.authservice.Context._

  def create(userId: UUID): Future[String] = {
    val now = new Date().getTime
    val token = Token(UUID.randomUUID.toString, userId, now.toString, None, now.toString)
    db.run(tokensTable += token).map(_ => token.token)
  }
}
