package de.thm.arsnova.gateway.api.contentservice

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.actor.Props
import akka.http.scaladsl.server.Directives._
import spray.json._
import de.thm.arsnova.gateway.api.BaseApi
import de.thm.arsnova.shared.Exceptions._
import de.thm.arsnova.shared.entities.Content
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.gateway.AuthServiceClientActor
import de.thm.arsnova.shared.servicecommands.AuthCommands.AuthenticateUser

trait ContentApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.ContentJsonProtocol._

  val contentApi = pathPrefix("room") {
    pathPrefix(JavaUUID) { roomId =>
      pathPrefix("content") {
        pathPrefix(JavaUUID) { contentId =>
          get {
            complete {
              (contentRegion ? GetContent(roomId, contentId))
                .mapTo[Option[Content]].map {
                case Some(c) => Success(c)
                case None => Failure(ResourceNotFound("content"))
              }
            }
          } ~
          delete {
            headerValueByName("X-Session-Token") { token =>
              complete {
                (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                  case Success(uId) => {
                    (contentRegion ? DeleteContent(roomId, contentId, uId))
                      .mapTo[Try[Content]]
                  }
                  case Failure(t) => Future.failed(t)
                }
              }
            }
          }
        } ~
        get {
          parameters("group") { group =>
            complete {
              (contentRegion ? GetContentListByRoomIdAndGroup(roomId, group))
                .mapTo[Seq[Content]].map(_.toJson)
            }
          }
        } ~
        get {
          complete {
            (contentRegion ? GetContentListByRoomId(roomId))
              .mapTo[Seq[Content]].map(_.toJson)
          }
        } ~
        post {
          headerValueByName("X-Session-Token") { token =>
            entity(as[Content]) { content =>
              complete {
                (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                  case Success(uId) => {
                    val withIds = content.copy(roomId = roomId, id = Some(UUID.randomUUID()))
                    (contentRegion ? CreateContent(roomId, withIds, uId))
                      .mapTo[Try[Content]]
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
}
