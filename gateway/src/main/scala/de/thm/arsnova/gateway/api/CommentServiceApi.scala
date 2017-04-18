package de.thm.arsnova.gateway.api

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.shared.entities.Comment
import de.thm.arsnova.shared.commands.CommentCommands._

trait CommentServiceApi {
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.CommentJsonProtocol._
  import de.thm.arsnova.gateway.Context._

  implicit val timeoutComment = Timeout(10.seconds)
  val remoteComment = system.actorSelection("akka://CommentService@127.0.0.1:9003/user/dispatcher")

  val commentServiceApi = pathPrefix("session") {
    pathPrefix(JavaUUID) { sessionId =>
      pathPrefix("comment") {
        pathPrefix(JavaUUID) { commentId =>
          get {
            complete {
              (remoteComment ? GetComment(commentId))
                .mapTo[Comment].map(_.toJson)
            }
          }
        } ~
        get {
          complete {
            (remoteComment ? GetCommentBySessionId(sessionId))
              .mapTo[Seq[Comment]].map(_.toJson)
          }
        } ~
        post {
          entity(as[Comment]) { comment =>
            complete {
              (remoteComment ? CreateComment(comment))
                .mapTo[UUID].map(_.toJson)
            }
          }
        }
      }
    }
  }
}
