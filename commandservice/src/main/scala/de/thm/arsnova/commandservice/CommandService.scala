package de.thm.arsnova.commandservice

import akka.actor.{Props}

object CommandService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[AuthActor], name = "auth")
}