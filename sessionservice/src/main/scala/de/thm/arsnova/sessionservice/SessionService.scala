package de.thm.arsnova.sessionservice

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

object SessionService extends App {
  val config = ConfigFactory.load

  implicit val system = ActorSystem("SessionService", config)
  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(2 minutes)

  val dispatcher = system.actorOf(Props[DispatcherActor], name = "dispatcher")
}