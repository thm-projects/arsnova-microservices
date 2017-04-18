package de.thm.arsnova.sessionservice

import akka.actor.{Props}

object SessionService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
}