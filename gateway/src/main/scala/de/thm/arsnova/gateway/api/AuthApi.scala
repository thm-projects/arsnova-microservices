package de.thm.arsnova.gateway.api

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.shared.commands.AuthCommands._
import de.thm.arsnova.shared.entities.{User, Token}

trait AuthApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.UserJsonProtocol._

  val authApi = pathPrefix("auth") {
    pathPrefix("whoami") {
      get {
        headerValueByName("X-Session-Token") { tokenstring =>
          complete {
            (remoteAuthActor ? CheckTokenString(tokenstring))
              .mapTo[Boolean].map(_.toJson)
          }
        }
      }
    } ~
    get {
      parameters("username", "password") { (username, password) =>
        complete {
          (remoteAuthActor ? LoginUser(username, password))
            .mapTo[String].map(_.toJson)
        }
      }
    } ~
    post {
      entity(as[User]) { user =>
        complete {
          (remoteAuthActor ? CreateUser(user))
            .mapTo[UUID].map(_.toJson)
        }
      }
    }
  }
}
