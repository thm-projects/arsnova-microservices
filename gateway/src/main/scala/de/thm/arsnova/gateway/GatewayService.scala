package de.thm.arsnova.gateway

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

object GatewayService extends App with Config with Routes {
  import Context._

  Http().bindAndHandle(handler = logRequestResult("log")(routes), interface = httpInterface, port = httpPort)
}
