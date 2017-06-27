package de.thm.arsnova.authservice

import akka.actor.Props

import de.thm.arsnova.shared.actors.ServiceManagementActor

object AuthService extends App {
  import Context._

  val auth = system.actorOf(Props[AuthActor], name = "auth")
  val manager = system.actorOf(ServiceManagementActor.props("auth", auth), "manager")
}
