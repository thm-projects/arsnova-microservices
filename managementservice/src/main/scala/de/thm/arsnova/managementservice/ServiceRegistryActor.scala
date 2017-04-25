package main.scala.de.thm.arsnova.managementservice

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe

import de.thm.arsnova.shared.commands.ServiceCommands._

class ServiceRegistryActor extends Actor {
  def receive = {
    case RegisterService(path) =>
  }
}
