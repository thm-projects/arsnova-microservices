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
import de.thm.arsnova.shared.Exceptions._
import spray.json._
import de.thm.arsnova.shared.servicecommands.KeywordCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.servicecommands.AuthCommands.AuthenticateUser
import de.thm.arsnova.shared.shards.SessionShard

/*
The API Interface regarding sessions, the core component for arsnova.voting.
 */
trait SessionServiceApi extends BaseApi {
  import de.thm.arsnova.gateway.Context._
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  val sessionList = system.actorOf(Props[SessionListClientActor], name = "sessionlist")

  val sessionApi = pathPrefix("session") {
    pathEndOrSingleSlash {
      post {
        headerValueByName("X-Session-Token") { token =>
          entity(as[Session]) { session =>
            complete {
              (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                case Success(uId) => {
                  (sessionList ? GenerateEntry).mapTo[SessionListEntry].map { s =>
                    val completeSession = session.copy(id = Some(s.id), keyword = Some(s.keyword))
                    (sessionRegion ? CreateSession(completeSession.id.get, completeSession, uId))
                      .mapTo[Try[Session]]
                  }
                }
                case Failure(t) => Future.failed(t)
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
        headerValueByName("X-Session-Token") { token =>
          parameter("userid") { userId =>
            complete {
              (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                case Success(uId) => {
                  if (userId == uId) {
                    (userRegion ? GetUserSessions(UUID.fromString(userId)))
                      .mapTo[Try[Seq[Session]]]
                  } else {
                    Future.failed(InvalidToken(token))
                  }
                }
                case Failure(t) => Future.failed(t)
              }
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
          headerValueByName("X-Session-Token") { token =>
            entity(as[Session]) { session =>
              complete {
                (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                  case Success(uId) => {
                    (sessionRegion ? UpdateSession(sessionId, session, uId))
                      .mapTo[Try[Session]]
                  }
                  case Failure(t) => Future.failed(t)
                }
              }
            }
          }
        } ~
        delete {
          headerValueByName("X-Session-Token") { token =>
            complete {
              (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                case Success(uId) => {
                  (sessionRegion ? DeleteSession(sessionId, uId))
                    .mapTo[Try[Session]]
                }
                case Failure(t) => Future.failed(t)
              }
            }
          }
        }
      }
    }
  }
}
