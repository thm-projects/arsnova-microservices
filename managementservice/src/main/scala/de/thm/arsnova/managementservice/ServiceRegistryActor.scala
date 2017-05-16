package de.thm.arsnova.managementservice

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe

import de.thm.arsnova.shared.management.RegistryCommands._

class ServiceRegistryActor extends Actor {
  val remoteCommander = context.actorSelection("akka://CommandService@127.0.0.1:8880/user/router")

  val services = collection.mutable.Map[String, ActorRef]()

  def receive = {
    case RegisterService(serviceType, remote) =>
      println(s"$serviceType has registered")
      services(serviceType) = remote
      remoteCommander ! RegisterService(serviceType, remote)
  }
}
