package de.thm.arsnova.managementservice

import akka.actor.{Props}

object ManagementService extends App {
  import Context._

  val registry = system.actorOf(Props[ServiceRegistryActor], name = "registry")
}