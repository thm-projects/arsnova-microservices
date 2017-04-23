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
import de.thm.arsnova.shared.entities.User

trait AuthApi {
  import de.thm.arsnova.gateway.Context._
  import de.thm.arsnova.shared.mappings.UserJsonProtocol._

  implicit val timeoutAuth = Timeout(10.seconds)
  val remoteAuth = system.actorSelection("akka://CommandService@127.0.0.1:8880/user/auth")

  val authApi = pathPrefix("auth") {
    pathPrefix("whoami") {
      headerValueByName("X-Session-Token") { tokenstring =>
        complete {
          (remoteAuth ? CheckTokenString(tokenstring))
            .mapTo[Boolean].map(_.toJson)
        }
      }
    } ~
    get {
      parameters("username", "password") { (username, password) =>
        complete {
          (remoteAuth ? LoginUser(username, password))
            .mapTo[String].map(_.toJson)
        }
      }
    } ~
    post {
      entity(as[User]) { user =>
        complete {
          (remoteAuth ? CreateUser(user))
            .mapTo[UUID].map(_.toJson)
        }
      }
    }
  }
}
