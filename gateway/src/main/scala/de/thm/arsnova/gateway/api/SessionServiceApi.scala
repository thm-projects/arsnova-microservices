package de.thm.arsnova.gateway.api

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.shared.entities.Session
import de.thm.arsnova.shared.servicecommands.SessionCommands._
import de.thm.arsnova.shared.servicecommands.CommandWithToken

/*
The API Interface regarding sessions, the core component for arsnova.voting.
 */
trait SessionServiceApi extends BaseApi {
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  val sessionApi = pathPrefix("session") {
    optionalHeaderValueByName("X-Session-Token") { tokenstring =>
      pathEndOrSingleSlash {
        post {
          entity(as[Session]) { session =>
            complete {
              (remoteCommander ? CommandWithToken(CreateSession(session), tokenstring))
                .mapTo[UUID].map(_.toJson)
            }
          }
        } ~
        get {
          parameters("keyword") { keyword =>
            complete {
              (remoteCommander ? CommandWithToken(GetSessionByKeyword(keyword), tokenstring))
                .mapTo[Session].map(_.toJson)
            }
          }
        }
      } ~
      pathPrefix(JavaUUID) { sessionId =>
        pathEndOrSingleSlash {
          get {
            complete {
              (remoteCommander ? CommandWithToken(GetSession(sessionId), tokenstring))
                .mapTo[Session].map(_.toJson)
            }
          }
        }
      }
    }
  }
}
