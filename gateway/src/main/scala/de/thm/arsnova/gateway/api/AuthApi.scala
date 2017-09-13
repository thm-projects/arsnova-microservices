package de.thm.arsnova.gateway.api

import java.util.UUID

import akka.actor.Props
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.util.Try
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.routing.RandomPool
import de.thm.arsnova.gateway.Context._
import de.thm.arsnova.gateway.AuthServiceClientActor
import spray.json._
import de.thm.arsnova.shared.servicecommands.AuthCommands._
import de.thm.arsnova.shared.entities.{Token, User}

trait AuthApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.UserJsonProtocol._

  val authApi = pathPrefix("auth") {
    pathPrefix("whoami") {
      get {
        headerValueByName("X-Session-Token") { tokenstring =>
          complete {
            (authClient ? GetUserFromTokenString(tokenstring))
              .mapTo[Try[User]]
          }
        }
      }
    } ~
    get {
      parameters("username", "password") { (username, password) =>
        complete {
          (authClient ? LoginUser(username, password))
            .mapTo[String].map(_.toJson)
        }
      }
    }
  }
}
