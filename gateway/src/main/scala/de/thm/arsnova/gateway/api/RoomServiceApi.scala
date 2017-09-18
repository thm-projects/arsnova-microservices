package de.thm.arsnova.gateway.api

import de.thm.arsnova.gateway.RoomListClientActor
import de.thm.arsnova.shared.entities.{Room, RoomListEntry}
import de.thm.arsnova.shared.servicecommands.RoomCommands._
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.roomservice.RoomActor
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
import de.thm.arsnova.shared.shards.RoomShard

/*
The API Interface regarding rooms, the core component for arsnova.voting.
 */
trait RoomServiceApi extends BaseApi {
  import de.thm.arsnova.gateway.Context._
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.RoomJsonProtocol._

  val roomList = system.actorOf(Props[RoomListClientActor], name = "roomlist")

  val roomApi = pathPrefix("room") {
    pathEndOrSingleSlash {
      post {
        headerValueByName("X-Session-Token") { token =>
          entity(as[Room]) { room =>
            complete {
              (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                case Success(uId) => {
                  (roomList ? GenerateEntry).mapTo[RoomListEntry].map { s =>
                    val completeRoom = room.copy(id = Some(s.id), keyword = Some(s.keyword))
                    (roomRegion ? CreateRoom(completeRoom.id.get, completeRoom, uId))
                      .mapTo[Try[Room]]
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
            (roomList ? LookupRoom(keyword)).mapTo[Option[UUID]].map {
              case Some(sid) =>
                (roomRegion ? GetRoom(sid))
                  .mapTo[Try[Room]]
              case None => Future.successful(Failure(NoSuchRoom(Right(keyword))))
            }
          }
        } ~
        headerValueByName("X-Session-Token") { token =>
          parameter("userid") { userId =>
            complete {
              (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                case Success(uId) => {
                  if (UUID.fromString(userId) == uId) {
                    (userRegion ? GetUserRooms(UUID.fromString(userId)))
                      .mapTo[Try[Seq[Room]]]
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
    pathPrefix(JavaUUID) { roomId =>
      pathEndOrSingleSlash {
        get {
          complete {
            (roomRegion ? GetRoom(roomId))
              .mapTo[Try[Room]]
          }
        } ~
        put {
          headerValueByName("X-Session-Token") { token =>
            entity(as[Room]) { room =>
              complete {
                (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                  case Success(uId) => {
                    (roomRegion ? UpdateRoom(roomId, room, uId))
                      .mapTo[Try[Room]]
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
                  (roomRegion ? DeleteRoom(roomId, uId))
                    .mapTo[Try[Room]]
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
