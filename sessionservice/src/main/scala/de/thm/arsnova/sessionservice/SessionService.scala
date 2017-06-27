package de.thm.arsnova.sessionservice

import akka.actor.{Props}

import de.thm.arsnova.shared.actors.ServiceManagementActor

object SessionService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
  val manager = system.actorOf(ServiceManagementActor.props("session", dispatcher), name = "manager")
}