package de.thm.arsnova.gateway.api

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.shared.commands.AuthCommands._

trait AuthApi {
  import de.thm.arsnova.gateway.Context._

  implicit val timeoutAuth = Timeout(10.seconds)
  val remoteAuth = system.actorSelection("akka://CommandService@127.0.0.1:8880/user/auth")

  val authApi = pathPrefix("auth") {
    get {
      parameters("username", "password") { (username, password) =>
        complete {
          (remoteAuth ? LoginUser(username, password))
            .mapTo[String].map(_.toJson)
        }
      }
    }
  }
}
