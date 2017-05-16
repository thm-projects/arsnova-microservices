package de.thm.arsnova.commandservice

import akka.actor.{Props}

object CommandService extends App {
  import Context._

  val router = system.actorOf(Props[RoutingActor], name = "router")
  val commander = system.actorOf(Props[CommandActor], name = "commander")
}