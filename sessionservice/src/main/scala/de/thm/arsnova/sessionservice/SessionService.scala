package de.thm.arsnova.sessionservice

import akka.actor.Props
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.routing.{ConsistentHashingPool, RandomPool}
import kamon.Kamon
import de.thm.arsnova.authservice.AuthServiceActor
import de.thm.arsnova.shared.actors.ServiceManagementActor

object SessionService extends App with MigrationConfig {
  import Context._

  Kamon.start()

  val authRouter = system.actorOf(
    ClusterRouterPool(new RandomPool(10), ClusterRouterPoolSettings(
      totalInstances = 10,
      maxInstancesPerNode = 5,
      allowLocalRoutees = false,
      useRole = Some("auth")
    )).props(Props[AuthServiceActor]), "AuthRouter"
  )

  if (args.contains("migrate")) {
    migrate()
  }
  if (args.contains("cleanDB")) {
    reloadSchema()
  }

  // val manager = system.actorOf(ServiceManagementActor.props("session", dispatcher), "manager")
}