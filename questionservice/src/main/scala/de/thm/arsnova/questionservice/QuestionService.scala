package de.thm.arsnova.questionservice

import akka.actor.{Props}

object QuestionService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
}