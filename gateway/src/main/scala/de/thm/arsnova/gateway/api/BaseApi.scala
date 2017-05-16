package de.thm.arsnova.gateway.api

import scala.concurrent.duration._
import akka.util.Timeout

trait BaseApi {
  import de.thm.arsnova.gateway.Context.system

  // TODO: why do i need a new implicit val?
  implicit val executionContext = system.dispatcher

  // timeout for actor calls
  implicit val timeout = Timeout(10.seconds)
  // actor for every command
  val remoteCommander = system.actorSelection("akka://CommandService@127.0.0.1:8880/user/commander")
}
