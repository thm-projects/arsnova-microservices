package de.thm.arsnova.shared.actors

import akka.actor.Actor
import akka.actor.ActorRef

import de.thm.arsnova.shared.management.RegistryCommands._

class ServiceManagementActor extends Actor {

  val registry = context.actorSelection("akka://ManagementService@127.0.0.1:8870/user/registry")

  def receive = {
    case RegisterService(serviceType, remote) =>
      registry ! RegisterService(serviceType, remote)
  }
}
