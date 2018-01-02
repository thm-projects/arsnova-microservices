package de.thm.arsnova.gateway.api

import java.util.UUID

import akka.actor.Props
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import de.thm.arsnova.gateway.RoomListClientActor
import de.thm.arsnova.shared.entities.export.RoomExport
import de.thm.arsnova.shared.servicecommands.AuthCommands.AuthenticateUser
import de.thm.arsnova.shared.servicecommands.RoomCommands._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait RoomExportServiceApi extends BaseApi {
  import de.thm.arsnova.gateway.Context._
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.export.RoomExportJsonProtocol._

  val roomList = system.actorOf(Props[RoomListClientActor], name = "roomlist")

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
    }
  }
}
