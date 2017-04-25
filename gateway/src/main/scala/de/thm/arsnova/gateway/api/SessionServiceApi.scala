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
import de.thm.arsnova.shared.commands.SessionCommands._

/*
The API Interface regarding sessions, the core component for arsnova.voting.
 */
trait SessionServiceApi extends BaseApi {
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  val sessionApi = pathPrefix("session") {
    pathEndOrSingleSlash {
      post {
        entity(as[Session]) { session =>
          complete {
            (remote ? CreateSession(session)).mapTo[UUID].map(_.toJson)
          }
        }
      } ~
      get {
        parameters("keyword") { keyword =>
          complete {
            (remote ? GetSessionByKeyword(keyword)).mapTo[Session].map(_.toJson)
          }
        }
      }
    } ~
    pathPrefix(JavaUUID) { sessionId =>
      pathEndOrSingleSlash {
        get {
          complete {
            (remote ? GetSession(sessionId)).mapTo[Session].map(_.toJson)
          }
        }/* ~
          put {
            entity(as[Session]) { session =>
              complete (SessionService.update(session, sessionId).map(_.toJson))
            }
          } ~
          delete {
            complete (SessionService.delete(sessionId).map(_.toJson))
          }*/
      }
    }
  }
}
