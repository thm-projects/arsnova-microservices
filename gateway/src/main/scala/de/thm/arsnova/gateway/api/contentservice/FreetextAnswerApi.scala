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
  import de.thm.arsnova.shared.mappings.EntitiesJsonProtocol._

  val freetextAnswerApi = pathPrefix("room") {
    optionalHeaderValueByName("X-Session-Token") { tokenstring =>
      pathPrefix(JavaUUID) { roomId =>
        pathPrefix("content") {
          pathPrefix(JavaUUID) { contentId =>
            pathPrefix("freetextanswer") {
              pathPrefix(JavaUUID) { answerId =>
                get {
                  complete {
                    (answerListRegion ? GetFreetextAnswer(contentId, answerId))
                      .mapTo[Option[FreetextAnswer]]
                  }
                } ~
                delete {
                  headerValueByName("X-Session-Token") { token =>
                    complete {
                      (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                        case Success(uId) => {
                          (answerListRegion ? DeleteFreetextAnswer(contentId, roomId, answerId, uId))
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
                  (answerListRegion ? GetFreetextAnswers(contentId))
                    .mapTo[Seq[FreetextAnswer]]
                }
              } ~
              post {
                headerValueByName("X-Session-Token") { token =>
                  entity(as[FreetextAnswer]) { answer =>
                    complete {
                      (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                        case Success(uId) => {
                          (answerListRegion ? CreateFreetextAnswer(contentId, roomId, answer, uId))
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
