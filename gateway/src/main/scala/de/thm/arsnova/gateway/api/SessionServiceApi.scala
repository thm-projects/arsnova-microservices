package de.thm.arsnova.gateway.api

import de.thm.arsnova.gateway.SessionListClientActor
import de.thm.arsnova.shared.entities.{Session, SessionListEntry}
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
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.routing.RandomPool
import akka.routing.RandomGroup
import de.thm.arsnova.shared.Exceptions.NoSuchSession
import spray.json._
import de.thm.arsnova.shared.servicecommands.KeywordCommands._
import de.thm.arsnova.shared.shards.SessionShard

/*
The API Interface regarding sessions, the core component for arsnova.voting.
 */
trait SessionServiceApi extends BaseApi {
  import de.thm.arsnova.gateway.Context._
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  ClusterSharding(system).startProxy(
    typeName = SessionShard.shardName,
    role = Some("session"),
    extractEntityId = SessionShard.idExtractor,
    extractShardId = SessionShard.shardResolver)

  val sessionRegion = ClusterSharding(system).shardRegion(SessionShard.shardName)

  val sessionList = system.actorOf(Props[SessionListClientActor], name = "sessionlist")

  val sessionApi = pathPrefix("session") {
    pathEndOrSingleSlash {
      post {
        headerValueByName("X-Session-Token") { tokenstring =>
          entity(as[Session]) { session =>
            complete {
              (sessionList ? GenerateEntry).mapTo[SessionListEntry].map { s =>
                val completeSession = session.copy(id = Some(s.id), keyword = Some(s.keyword))
                (sessionRegion ? CreateSession(completeSession.id.get, completeSession, tokenstring))
                  .mapTo[Session].map(_.toJson)
              }
            }
          }
        }
      } ~
      get {
        parameter("keyword") { keyword =>
          complete {
            (sessionList ? LookupSession(keyword)).mapTo[Option[UUID]].map {
              case Some(sid) =>
                (sessionRegion ? GetSession(sid))
                  .mapTo[Option[Session]].map {
                  case Some(s) => s.toJson
                  case None => NoSuchSession(Left(sid)).toJson
                }
              case None => Future.successful(NoSuchSession(Right(keyword))).map(_.toJson)
            }
          }
        }
      }
    } ~
    pathPrefix(JavaUUID) { sessionId =>
      pathEndOrSingleSlash {
        get {
          complete {
            (sessionRegion ? GetSession(sessionId))
              .mapTo[Option[Session]].map {
              case Some(s) => s.toJson
              case None => NoSuchSession(Left(sessionId)).toJson
            }
          }
        }
      }
    }
  }
}
