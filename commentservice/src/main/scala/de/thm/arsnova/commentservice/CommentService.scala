package de.thm.arsnova.commentservice

import akka.actor.{Props}

import de.thm.arsnova.shared.actors.ServiceManagementActor

object CommentService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
  val manager = system.actorOf(ServiceManagementActor.props("comment", dispatcher), "manager")
}