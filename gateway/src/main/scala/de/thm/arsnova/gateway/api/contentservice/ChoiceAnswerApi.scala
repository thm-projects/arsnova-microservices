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
import de.thm.arsnova.gateway.sharding.AnswerListShard

trait ChoiceAnswerApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.ChoiceAnswerJsonProtocol._

  val choiceAnswerListRegion = AnswerListShard.getProxy

  val choiceAnswerApi = pathPrefix("session") {
    pathPrefix(JavaUUID) { sessionId =>
      pathPrefix("question") {
        pathPrefix(JavaUUID) { questionId =>
          pathPrefix("choiceanswer") {
            pathPrefix(JavaUUID) { answerId =>
              get {
                complete {
                  (choiceAnswerListRegion ? GetChoiceAnswer(sessionId, questionId, answerId))
                    .mapTo[Try[ChoiceAnswer]]
                }
              }
            } ~
            get {
              complete {
                (choiceAnswerListRegion ? GetChoiceAnswers(sessionId, questionId))
                  .mapTo[Seq[ChoiceAnswer]].map(_.toJson)
              }
            } ~
            post {
              headerValueByName("X-Session-Token") { tokenstring =>
                entity(as[ChoiceAnswer]) { answer =>
                  complete {
                    (choiceAnswerListRegion ? CreateChoiceAnswer(sessionId, questionId, answer, tokenstring))
                      .mapTo[Try[ChoiceAnswer]].map(_.toJson)
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
