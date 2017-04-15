package de.thm.arsnova.gateway.api

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.shared.entities.Session
import de.thm.arsnova.shared.commands.SessionCommands._

/*
The API Interface regarding sessions, the core component for arsnova.voting.
 */
trait SessionApi {
  // protocol for serializing data
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._
  import de.thm.arsnova.gateway.Context._

  implicit val timeout = Timeout(5.seconds)

  val sessionApi = pathPrefix("session") {
    /*pathEndOrSingleSlash {
      post {
        entity(as[Session]) { session =>
          complete (SessionService.create(session).map(_.toJson))
        }
      }
    } ~*/
    pathPrefix(JavaUUID) { sessionId =>
      pathEndOrSingleSlash {
        get {
          complete {
            val remote = system.actorSelection("akka.tcp://SessionService@127.0.0.1:9001/user/dispatcher")
            (remote ? GetSession(sessionId)).asInstanceOf[Future[Session]].map (_.toJson)
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
