package de.thm.arsnova.gateway.api

import de.thm.arsnova.shared.entities.Session
import de.thm.arsnova.shared.servicecommands.SessionCommands._
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import java.util.UUID

import akka.actor.Props
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.routing.RandomPool
import akka.routing.RandomGroup
import de.thm.arsnova.sessionservice.SessionServiceActor
import spray.json._

/*
The API Interface regarding sessions, the core component for arsnova.voting.
 */
trait SessionServiceApi extends BaseApi {
  import de.thm.arsnova.gateway.Context._
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  val sessionRouter = system.actorOf(
    ClusterRouterPool(new RandomPool(10), ClusterRouterPoolSettings(
      totalInstances = 10,
      maxInstancesPerNode = 10,
      allowLocalRoutees = false,
      useRole = Some("session")
    )).props(Props[SessionServiceActor]), "SessionRouter"
  )

  /*val sessionRouter = system.actorOf(
    ClusterRouterGroup(RandomGroup(Nil), ClusterRouterGroupSettings(
      totalInstances = 100, routeesPaths = List("/user/sessionWorker"),
      allowLocalRoutees = false, useRole = Some("session"))).props(),
    name = "workerRouter2")*/

  val sessionApi = pathPrefix("session") {
    optionalHeaderValueByName("X-Session-Token") { tokenstring =>
      pathEndOrSingleSlash {
        post {
          entity(as[Session]) { session =>
            complete {
              (sessionRouter ? CommandWithToken(CreateSession(session), tokenstring))
                .mapTo[UUID].map(_.toJson)
            }
          }
        } ~
        get {
          parameters("keyword") { keyword =>
            complete {
              (sessionRouter ? CommandWithToken(GetSessionByKeyword(keyword), tokenstring))
                .mapTo[Session].map(_.toJson)
            }
          }
        }
      } ~
      pathPrefix(JavaUUID) { sessionId =>
        pathEndOrSingleSlash {
          get {
            complete {
              (sessionRouter ? CommandWithToken(GetSession(sessionId), tokenstring))
                .mapTo[Session].map(_.toJson)
            }
          }
        }
      }
    }
  }
}
