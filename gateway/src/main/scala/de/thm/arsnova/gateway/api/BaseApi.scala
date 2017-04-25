package de.thm.arsnova.gateway.api

import scala.concurrent.duration._
import akka.util.Timeout

trait BaseApi {
  import de.thm.arsnova.gateway.Context._

  // timeout for actor calls
  implicit val timeout = Timeout(10.seconds)
  // actor in the commandservice that handles auth
  val remoteAuthorizer = system.actorSelection("akka://CommandService@127.0.0.1:8880/user/auth")
  // actor for every command besides authcommands
  val remoteCommander = system.actorSelection("akka://CommandService@127.0.0.1:8880/user/commander")
}
