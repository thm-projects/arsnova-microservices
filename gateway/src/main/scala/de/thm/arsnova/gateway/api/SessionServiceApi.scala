package de.thm.arsnova.gateway.api

import de.thm.arsnova.shared.entities.Session
import de.thm.arsnova.shared.servicecommands.SessionCommands._
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.sessionservice.SessionActor

import java.util.UUID

import akka.actor.Props
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.cluster.sharding.ClusterSharding

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.routing.RandomPool
import akka.routing.RandomGroup
import spray.json._

/*
The API Interface regarding sessions, the core component for arsnova.voting.
 */
trait SessionServiceApi extends BaseApi {
  import de.thm.arsnova.gateway.Context._
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  ClusterSharding(system).startProxy(
    typeName = SessionActor.shardName,
    role = Some("session"),
    extractEntityId = SessionActor.idExtractor,
    extractShardId = SessionActor.shardResolver)

  val sessionRegion = ClusterSharding(system).shardRegion(SessionActor.shardName)

  val sessionApi = pathPrefix("session") {
    optionalHeaderValueByName("X-Session-Token") { tokenstring =>
      pathEndOrSingleSlash {
        post {
          entity(as[Session]) { session =>
            complete {
              val newId = UUID.randomUUID()
              (sessionRegion ? CreateSession(newId, session.copy(id = Some(newId)), tokenstring))
                .mapTo[UUID].map(_.toJson)
            }
          }
        }
      } ~
      pathPrefix(JavaUUID) { sessionId =>
        pathEndOrSingleSlash {
          get {
            complete {
              (sessionRegion ? GetSession(sessionId))
                .mapTo[Session].map(_.toJson)
            }
          }
        }
      }
    }
  }
}
