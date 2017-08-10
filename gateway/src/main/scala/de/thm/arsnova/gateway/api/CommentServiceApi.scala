package de.thm.arsnova.gateway.api

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import de.thm.arsnova.gateway.sharding.CommentListShard
import spray.json._
import de.thm.arsnova.shared.entities.Comment
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.CommentCommands._

trait CommentServiceApi extends BaseApi {
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.CommentJsonProtocol._

  val commentRegion = CommentListShard.getProxy

  val commentServiceApi = pathPrefix("session") {
    pathPrefix(JavaUUID) { sessionId =>
      pathPrefix("comment") {
        pathPrefix(JavaUUID) { commentId =>
          get {
            complete {
              (commentRegion ? GetComment(sessionId, commentId))
                .mapTo[Comment].map(_.toJson)
            }
          }
        } ~
        get {
          parameter("unreadonly" ? false) { read =>
            complete {
              read match {
                case true => {
                  (commentRegion ? GetUnreadComments(sessionId))
                    .mapTo[Try[Seq[Comment]]]
                }
                case false => {
                  (commentRegion ? GetCommentsBySessionId(sessionId))
                    .mapTo[Seq[Comment]].map(_.toJson)
                }
              }
            }
          }
        } ~
        post {
          optionalHeaderValueByName("X-Session-Token") { tokenstring =>
            entity(as[Comment]) { comment =>
              complete {
                (commentRegion ? CreateComment(sessionId, comment))
                  .mapTo[UUID].map(_.toJson)
              }
            }
          }
        }
      }
    }
  }
}
