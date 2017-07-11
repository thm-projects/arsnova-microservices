package de.thm.arsnova.gateway

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import de.thm.arsnova.sessionservice.SessionActor
import kamon.Kamon

object GatewayService extends App with Config with Routes {
  import Context._


  if (args.contains("kamon")) {
    Kamon.start()
  }

  Http().bindAndHandle(handler = logRequestResult("log")(routes), interface = httpInterface, port = httpPort)
}
