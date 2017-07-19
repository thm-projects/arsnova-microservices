package de.thm.arsnova.shared.actors

import de.thm.arsnova.shared.management.RegistryCommands._
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ServiceManagementActor(serviceType: String, serviceActorRef: ActorRef) extends Actor with ActorLogging {
  var registry: Option[ActorRef] = None

  implicit val ec: ExecutionContext = context.dispatcher

  implicit val timeout: Timeout = 5.seconds

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
    case m @ GetActorRefForService(serviceType) => ((ret: ActorRef) => {
      registry match {
        case Some(r) => (r ? m).map {ret ! _}
        case None => {
          // TODO: return case class capsuling exception
          ret ! ""
        }
      }
    }) (sender)
  }
}

object ServiceManagementActor {
  def props(serviceType: String, serviceActorRef: ActorRef) = {
    Props(new ServiceManagementActor(serviceType, serviceActorRef))
  }
}
