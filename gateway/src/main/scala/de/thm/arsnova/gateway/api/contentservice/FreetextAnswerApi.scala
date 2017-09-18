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
import de.thm.arsnova.shared.servicecommands.AuthCommands.AuthenticateUser

trait FreetextAnswerApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.FreetextAnswerJsonProtocol._

  val freetextAnswerApi = pathPrefix("room") {
    optionalHeaderValueByName("X-Session-Token") { tokenstring =>
      pathPrefix(JavaUUID) { roomId =>
        pathPrefix("question") {
          pathPrefix(JavaUUID) { questionId =>
            pathPrefix("freetextanswer") {
              pathPrefix(JavaUUID) { answerId =>
                get {
                  complete {
                    (answerListRegion ? GetFreetextAnswer(roomId, questionId, answerId))
                      .mapTo[Option[FreetextAnswer]]
                  }
                } ~
                delete {
                  headerValueByName("X-Session-Token") { token =>
                    complete {
                      (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                        case Success(uId) => {
                          (answerListRegion ? DeleteFreetextAnswer(roomId, questionId, answerId, uId))
                            .mapTo[Try[FreetextAnswer]]
                        }
                        case Failure(t) => Future.failed(t)
                      }
                    }
                  }
                }
              } ~
              get {
                complete {
                  (answerListRegion ? GetFreetextAnswers(roomId, questionId))
                    .mapTo[Seq[FreetextAnswer]]
                }
              } ~
              post {
                headerValueByName("X-Session-Token") { token =>
                  entity(as[FreetextAnswer]) { answer =>
                    complete {
                      (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                        case Success(uId) => {
                          (answerListRegion ? CreateFreetextAnswer(roomId, questionId, answer, uId))
                            .mapTo[Try[FreetextAnswer]]
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
    }
  }
}
