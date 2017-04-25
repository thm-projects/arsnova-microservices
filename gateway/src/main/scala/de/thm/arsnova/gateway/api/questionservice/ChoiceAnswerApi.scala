package de.thm.arsnova.gateway.api.questionservice

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.gateway.api.BaseApi
import de.thm.arsnova.shared.entities.ChoiceAnswer
import de.thm.arsnova.shared.commands.ChoiceAnswerCommands._

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
                  (remoteChoiceAnswer ? GetChoiceAnswer(answerId))
                    .mapTo[ChoiceAnswer].map(_.toJson)
                }
              }
            }
            get {
              complete {
                (remoteChoiceAnswer ? GetChoiceAnswersByQuestionId(questionId))
                  .mapTo[Seq[ChoiceAnswer]].map(_.toJson)
              }
            } ~
            post {
              entity(as[ChoiceAnswer]) { answer =>
                complete {
                  (remoteChoiceAnswer ? CreateChoiceAnswer(answer))
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
