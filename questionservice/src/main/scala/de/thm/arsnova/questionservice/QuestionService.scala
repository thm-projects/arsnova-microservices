package de.thm.arsnova.questionservice

import akka.actor.{Props}

import de.thm.arsnova.shared.actors.ServiceManagementActor

object QuestionService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
  val manager = system.actorOf(ServiceManagementActor.props("question", dispatcher), name = "manager")
}