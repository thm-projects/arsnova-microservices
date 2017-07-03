package de.thm.arsnova.sessionservice

import akka.actor.Props
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.routing.ConsistentHashingPool
import de.thm.arsnova.authservice.AuthServiceActor
import de.thm.arsnova.shared.actors.ServiceManagementActor

object SessionService extends App with MigrationConfig {
  import Context._

  val authRouter = system.actorOf(
    ClusterRouterPool(new ConsistentHashingPool(0), ClusterRouterPoolSettings(
      totalInstances = 100,
      maxInstancesPerNode = 100,
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