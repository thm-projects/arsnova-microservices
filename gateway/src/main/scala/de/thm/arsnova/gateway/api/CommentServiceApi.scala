package de.thm.arsnova.gateway.api

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.shared.entities.Comment
import de.thm.arsnova.shared.servicecommands.CommentCommands._

trait CommentServiceApi extends BaseApi {
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.CommentJsonProtocol._

  val commentServiceApi = pathPrefix("session") {
    pathPrefix(JavaUUID) { sessionId =>
      pathPrefix("comment") {
        pathPrefix(JavaUUID) { commentId =>
          get {
            complete {
              (remoteCommander ? GetComment(commentId))
                .mapTo[Comment].map(_.toJson)
            }
          }
        } ~
        get {
          complete {
            (remoteCommander ? GetCommentBySessionId(sessionId))
              .mapTo[Seq[Comment]].map(_.toJson)
          }
        } ~
        post {
          entity(as[Comment]) { comment =>
            complete {
              (remoteCommander ? CreateComment(comment))
                .mapTo[UUID].map(_.toJson)
            }
          }
        }
      }
    }
  }
}
