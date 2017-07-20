package de.thm.arsnova.gateway.api.contentservice

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.gateway.api.BaseApi
import de.thm.arsnova.shared.entities.Content
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.ContentCommands._

trait ContentServiceApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.ContentJsonProtocol._

  val contentServiceApi = pathPrefix("session") {
    optionalHeaderValueByName("X-Session-Token") { tokenstring =>
      pathPrefix(JavaUUID) { sessionId =>
        pathPrefix("content") {
          pathPrefix(JavaUUID) { contentId =>
            get {
              complete {
                (remoteCommander ? CommandWithToken(GetContent(sessionId, contentId), tokenstring))
                  .mapTo[Content].map(_.toJson)
              }
            }
          } ~
          get {
            complete {
              (remoteCommander ? CommandWithToken(GetContentListBySessionId(sessionId), tokenstring))
                .mapTo[Seq[Content]].map(_.toJson)
            }
          } ~
          get {
            parameters("variant") { variant =>
              complete {
                (remoteCommander ? CommandWithToken(GetContentListBySessionIdAndVariant(sessionId, variant), tokenstring))
                  .mapTo[Seq[Content]].map(_.toJson)
              }
            }
          } ~
          post {
            entity(as[Content]) { content =>
              complete {
                (remoteCommander ? CommandWithToken(CreateContent(sessionId, content.copy(sessionId = sessionId)), tokenstring))
                  .mapTo[UUID].map(_.toJson)
              }
            }
          }
        }
      }
    }
  }
}
