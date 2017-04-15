package de.thm.arsnova.gateway

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

object Context {
  implicit val system = ActorSystem()
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  protected val log: LoggingAdapter = Logging(system, getClass)
}
