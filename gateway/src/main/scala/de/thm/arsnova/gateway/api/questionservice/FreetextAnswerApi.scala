package de.thm.arsnova.gateway.api

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.gateway.api.BaseApi
import de.thm.arsnova.shared.entities.FreetextAnswer
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands._

trait FreetextAnswerApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.FreetextAnswerJsonProtocol._

  val freetextAnswerApi = pathPrefix("session") {
    optionalHeaderValueByName("X-Session-Token") { tokenstring =>
      pathPrefix(JavaUUID) { sessionId =>
        pathPrefix("question") {
          pathPrefix(JavaUUID) { questionId =>
            pathPrefix("freetextanswer") {
              pathPrefix(JavaUUID) { answerId =>
                get {
                  complete {
                    (remoteCommander ? CommandWithToken(GetFreetextAnswer(answerId), tokenstring))
                      .mapTo[FreetextAnswer].map(_.toJson)
                  }
                }
              } ~
              get {
                complete {
                  (remoteCommander ? CommandWithToken(GetFreetextAnswersByQuestionId(questionId), tokenstring))
                    .mapTo[Seq[FreetextAnswer]].map(_.toJson)
                }
              } ~
              post {
                entity(as[FreetextAnswer]) { answer =>
                  complete {
                    (remoteCommander ? CommandWithToken(CreateFreetextAnswer(answer), tokenstring))
                      .mapTo[Int].map(_.toJson)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
