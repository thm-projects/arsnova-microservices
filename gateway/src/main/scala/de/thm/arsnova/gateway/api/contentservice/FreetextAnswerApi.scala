package de.thm.arsnova.gateway.api

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.Try
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._
import de.thm.arsnova.gateway.api.BaseApi
import de.thm.arsnova.shared.entities.FreetextAnswer
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
                    (answerListRegion ? GetFreetextAnswer(sessionId, questionId, answerId))
                      .mapTo[Option[FreetextAnswer]]
                  }
                } ~
                delete {
                  headerValueByName("X-Session-Token") { token =>
                    complete {
                      (answerListRegion ? DeleteFreetextAnswer(sessionId, questionId, answerId, token))
                        .mapTo[Try[FreetextAnswer]]
                    }
                  }
                }
              } ~
              get {
                complete {
                  (answerListRegion ? GetFreetextAnswers(sessionId, questionId))
                    .mapTo[Seq[FreetextAnswer]]
                }
              } ~
              post {
                headerValueByName("X-Session-Token") { token =>
                  entity(as[FreetextAnswer]) { answer =>
                    complete {
                      (answerListRegion ? CreateFreetextAnswer(sessionId, questionId, answer, token))
                        .mapTo[Try[FreetextAnswer]]
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
}
