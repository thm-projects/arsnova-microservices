package de.thm.arsnova.commandservice

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.Actor
import akka.actor.ActorRef

import de.thm.arsnova.shared.servicecommands._
import de.thm.arsnova.shared.management.CommandPackage

class RoutingActor extends Actor {
  val cache = collection.mutable.Map[String, ActorRef]()

  def receive = {
    case c: ServiceCommand => ((ret: ActorRef) => {
      (cache.get(c.getClass.getName)) match {
        case Some(ref) => ref ! CommandPackage(c, ret)
        case None => println("no service for this command")
      }
    }) (sender)
  }
}
