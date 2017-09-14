package de.thm.arsnova.gateway.api.contentservice

import java.util.UUID

import scala.util.Try
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._
import de.thm.arsnova.gateway.api.BaseApi
import de.thm.arsnova.shared.entities.ChoiceAnswer
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands._
import de.thm.arsnova.shared.servicecommands.CommandWithToken

trait ChoiceAnswerApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.ChoiceAnswerJsonProtocol._

  val choiceAnswerApi = pathPrefix("session") {
    pathPrefix(JavaUUID) { sessionId =>
      pathPrefix("question") {
        pathPrefix(JavaUUID) { questionId =>
          pathPrefix("choiceanswer") {
            pathPrefix(JavaUUID) { answerId =>
              get {
                complete {
                  (answerListRegion ? GetChoiceAnswer(sessionId, questionId, answerId))
                    .mapTo[Option[ChoiceAnswer]]
                }
              } ~
              delete {
                headerValueByName("X-Session-Token") { token =>
                  complete {
                    (answerListRegion ? DeleteChoiceAnswer(sessionId, questionId, answerId, token))
                      .mapTo[Try[ChoiceAnswer]]
                  }
                }
              }
            } ~
            get {
              complete {
                (answerListRegion ? GetChoiceAnswers(sessionId, questionId))
                  .mapTo[Seq[ChoiceAnswer]]
              }
            } ~
            post {
              headerValueByName("X-Session-Token") { tokenstring =>
                entity(as[ChoiceAnswer]) { answer =>
                  complete {
                    (answerListRegion ? CreateChoiceAnswer(sessionId, questionId, answer, tokenstring))
                      .mapTo[Try[ChoiceAnswer]]
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
