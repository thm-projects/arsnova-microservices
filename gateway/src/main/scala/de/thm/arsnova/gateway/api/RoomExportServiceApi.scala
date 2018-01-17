package de.thm.arsnova.gateway.api

import java.util.UUID

import akka.actor.Props
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import de.thm.arsnova.gateway.RoomListClientActor
import de.thm.arsnova.shared.entities.{Room, RoomListEntry}
import de.thm.arsnova.shared.entities.export.RoomExport
import de.thm.arsnova.shared.servicecommands.AuthCommands.AuthenticateUser
import de.thm.arsnova.shared.servicecommands.RoomCommands._
import de.thm.arsnova.shared.servicecommands.KeywordCommands._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait RoomExportServiceApi extends BaseApi {
  import de.thm.arsnova.gateway.Context._
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.export.RoomExportJsonProtocol._
  import de.thm.arsnova.shared.mappings.RoomJsonProtocol._

  val roomExportApi = pathPrefix("room") {
    pathPrefix(JavaUUID) { roomId =>
      pathPrefix("export") {
        get {
          headerValueByName("X-Session-Token") { token =>
            complete {
              (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                case Success(uId) => {
                  (roomRegion ? ExportRoom(roomId, uId))
                    .mapTo[Try[RoomExport]]
                }
                case Failure(t) => Future.failed(t)
              }
            }
          }
        }
      }
    } ~
    pathPrefix("import") {
      pathEndOrSingleSlash {
        post {
          headerValueByName("X-Session-Token") { token =>
            entity(as[RoomExport]) { room =>
              complete {
                (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                  case Success(uId) => {
                    (roomList ? GenerateEntry).mapTo[RoomListEntry].map { s =>
                      (roomRegion ? ImportRoom(s.id, s.keyword, uId, room))
                        .mapTo[Try[Room]]
                    }
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
