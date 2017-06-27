package de.thm.arsnova.shared.actors

import de.thm.arsnova.shared.management.RegistryCommands._
import akka.actor.Actor
import akka.actor.ActorRef

class ServiceManagementActor(serviceType: String, serviceActorRef: ActorRef) extends Actor {
  def receive = {
    case RequestRegistration =>
      println("manager got request to register service actors")
      sender ! RegisterService(serviceType, serviceActorRef)
  }
}
