package de.thm.arsnova.gateway

import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import de.thm.arsnova.sessionservice.SessionActor
import de.thm.arsnova.shared.actors.ServiceManagementActor
import kamon.Kamon

object GatewayService extends App with Config with Routes {
  import Context._

  if (args.contains("kamon")) {
    Kamon.start()
  }

  val manager = system.actorOf(ServiceManagementActor.props(Nil), "manager")

  Http().bindAndHandle(handler = logRequestResult("log")(routes), interface = httpInterface, port = httpPort)
}
