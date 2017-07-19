package de.thm.arsnova.authservice

import akka.actor.Props

import de.thm.arsnova.shared.actors.ServiceManagementActor

object AuthService extends App with MigrationConfig {
  import Context._

  val auth = system.actorOf(Props[AuthServiceActor], name = "auth")
  val manager = system.actorOf(ServiceManagementActor.props(Seq(("auth", auth))), "manager")

  if (args.contains("migrate")) {
    migrate()
  }
  if (args.contains("cleanDB")) {
    reloadSchema()
  }
}
