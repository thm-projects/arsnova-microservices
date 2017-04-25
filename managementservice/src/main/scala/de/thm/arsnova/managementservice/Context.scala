package de.thm.arsnova.managementservice

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.util.Timeout
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

object Context {
  val config = ConfigFactory.load

  implicit val system = ActorSystem("ManagementService", config)
  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(2 minutes)
  protected val log: LoggingAdapter = Logging(system, getClass)
}
