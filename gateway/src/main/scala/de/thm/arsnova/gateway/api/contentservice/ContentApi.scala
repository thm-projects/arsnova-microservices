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
import de.thm.arsnova.gateway.sharding.ContentShard
import de.thm.arsnova.shared.Exceptions.NoSuchContent
import de.thm.arsnova.shared.entities.Content
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.ContentCommands._

trait ContentApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.ContentJsonProtocol._

  val contentRegion = ContentShard.getProxy

  val contentApi = pathPrefix("session") {
    pathPrefix(JavaUUID) { sessionId =>
      pathPrefix("content") {
        pathPrefix(JavaUUID) { contentId =>
          get {
            complete {
              (contentRegion ? GetContent(sessionId, contentId))
                .mapTo[Option[Content]].map {
                case Some(c) => c.toJson
                case None => NoSuchContent.toJson
              }
            }
          }
        } ~
        get {
          complete {
            (contentRegion ? GetContentListBySessionId(sessionId))
              .mapTo[Seq[Content]].map(_.toJson)
          }
        } ~
        get {
          parameters("variant") { variant =>
            complete {
              (contentRegion ? GetContentListBySessionIdAndVariant(sessionId, variant))
                .mapTo[Seq[Content]].map(_.toJson)
            }
          }
        } ~
        post {
          optionalHeaderValueByName("X-Session-Token") { tokenstring =>
            entity(as[Content]) { content =>
              complete {
                val withIds = content.copy(sessionId = sessionId, id = Some(UUID.randomUUID()))
                (contentRegion ? CreateContent(sessionId, withIds, tokenstring))
                  .mapTo[Content].map(_.toJson)
              }
            }
          }
        }
      }
    }
  }
}
