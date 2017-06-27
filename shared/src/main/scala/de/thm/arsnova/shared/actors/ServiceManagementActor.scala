package de.thm.arsnova.shared.actors

import de.thm.arsnova.shared.management.RegistryCommands._
import akka.actor.{Actor, ActorLogging, ActorRef}

class ServiceManagementActor(serviceType: String, serviceActorRef: ActorRef) extends Actor with ActorLogging {
  var registry: Option[ActorRef] = None

  override def postStop(): Unit = {
    registry match {
      case Some(ref) => ref ! UnregisterService(serviceType, serviceActorRef)
      case None =>
    }
  }

  def receive = {
    case RequestRegistration =>
      registry = Some(sender)
      log.info("manager got request to register service actors")
      sender ! RegisterService(serviceType, serviceActorRef)
  }
}
