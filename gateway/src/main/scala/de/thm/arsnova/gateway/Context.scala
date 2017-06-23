package de.thm.arsnova.gateway

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

object Context {
  val config = ConfigFactory.load

  implicit val system = ActorSystem("ARSnovaService", config)
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  protected val log: LoggingAdapter = Logging(system, getClass)
}
