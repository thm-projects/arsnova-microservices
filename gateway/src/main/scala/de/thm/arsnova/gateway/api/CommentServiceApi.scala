package de.thm.arsnova.gateway.api

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._
import de.thm.arsnova.shared.entities.Comment
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.CommentCommands._
import de.thm.arsnova.shared.servicecommands.AuthCommands.AuthenticateUser

trait CommentServiceApi extends BaseApi {
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.CommentJsonProtocol._

  val commentServiceApi = pathPrefix("room") {
    pathPrefix(JavaUUID) { roomId =>
      pathPrefix("comment") {
        pathPrefix(JavaUUID) { commentId =>
          get {
            complete {
              (commentRegion ? GetComment(roomId, commentId))
                .mapTo[Try[Comment]]
            }
          }
        } ~
        get {
          parameter("unreadonly" ? false) { read =>
            complete {
              read match {
                case true => {
                  (commentRegion ? GetUnreadComments(roomId))
                    .mapTo[Try[Seq[Comment]]]
                }
                case false => {
                  (commentRegion ? GetCommentsByRoomId(roomId))
                    .mapTo[Seq[Comment]]
                }
              }
            }
          }
        } ~
        post {
          headerValueByName("X-Session-Token") { token =>
            entity(as[Comment]) { comment =>
              complete {
                (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                  case Success(uId) => {
                    (commentRegion ? CreateComment(roomId, comment, uId))
                      .mapTo[Try[Comment]]
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
