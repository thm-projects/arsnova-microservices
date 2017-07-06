package de.thm.arsnova.gateway

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import kamon.Kamon

object GatewayService extends App with Config with Routes {
  import Context._

  Kamon.start()

  Http().bindAndHandle(handler = logRequestResult("log")(routes), interface = httpInterface, port = httpPort)
}
