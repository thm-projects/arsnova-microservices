package de.thm.arsnova.authservice

import akka.actor.Props

import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.management.RegistryCommands.RegisterService

object AuthService extends App {
  import Context._

  val auth = system.actorOf(Props[AuthActor], name = "auth")
  //val manager = system.actorOf(Props[ServiceManagementActor], name = "manager")

  //manager ! RegisterService("AuthService", auth)
}
