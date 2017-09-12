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
import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.routing.RandomPool
import akka.routing.RandomGroup
import de.thm.arsnova.gateway.sharding.UserShard
import de.thm.arsnova.shared.Exceptions._
import spray.json._
import de.thm.arsnova.shared.servicecommands.KeywordCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands._
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
    role = SessionShard.serviceRole,
    extractEntityId = SessionShard.idExtractor,
    extractShardId = SessionShard.shardResolver)

  val sessionRegion = ClusterSharding(system).shardRegion(SessionShard.shardName)
  val sessionsUserRegion = UserShard.getProxy

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
                  .mapTo[Try[Session]]
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
                  .mapTo[Try[Session]]
              case None => Future.successful(Failure(NoSuchSession(Right(keyword))))
            }
          }
        } ~
        headerValueByName("X-Session-Token") { tokenstring =>
          parameter("userid") { userId =>
            complete {
              (sessionsUserRegion ? GetUserSessions(UUID.fromString(userId), tokenstring))
                .mapTo[Try[Seq[Session]]]
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
              .mapTo[Try[Session]]
          }
        } ~
        put {
          headerValueByName("X-Session-Token") { tokenstring =>
            entity(as[Session]) { session =>
              complete {
                (sessionRegion ? UpdateSession(sessionId, session, tokenstring))
                  .mapTo[Try[Session]]
              }
            }
          }
        } ~
        delete {
          headerValueByName("X-Session-Token") { tokenstring =>
            complete {
              (sessionRegion ? DeleteSession(sessionId, tokenstring))
                .mapTo[Try[Session]]
            }
          }
        }
      }
    }
  }
}
