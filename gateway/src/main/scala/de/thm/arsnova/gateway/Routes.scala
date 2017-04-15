package de.thm.arsnova.gateway

import akka.http.scaladsl.server.Directives._
import de.thm.arsnova.gateway.api._

trait Routes extends SessionApi {
  val routes = {
    sessionApi
  }
}
