package de.thm.arsnova.commentservice

import akka.actor.{Props}

object CommentService extends App {
  import Context._

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
}