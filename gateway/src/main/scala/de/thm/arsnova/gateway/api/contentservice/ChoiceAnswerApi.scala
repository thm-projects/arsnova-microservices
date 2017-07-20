package de.thm.arsnova.gateway.api.contentservice

import java.util.UUID

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
    optionalHeaderValueByName("X-Session-Token") { tokenstring =>
      pathPrefix(JavaUUID) { sessionId =>
        pathPrefix("question") {
          pathPrefix(JavaUUID) { questionId =>
            pathPrefix("choiceanswer") {
              pathPrefix(JavaUUID) { answerId =>
                get {
                  complete {
                    (remoteCommander ? CommandWithToken(GetChoiceAnswer(answerId), tokenstring))
                      .mapTo[ChoiceAnswer].map(_.toJson)
                  }
                }
              } ~
              get {
                complete {
                  (remoteCommander ? CommandWithToken(GetChoiceAnswersByQuestionId(questionId), tokenstring))
                    .mapTo[Seq[ChoiceAnswer]].map(_.toJson)
                }
              } ~
              post {
                entity(as[ChoiceAnswer]) { answer =>
                  complete {
                    (remoteCommander ? CommandWithToken(CreateChoiceAnswer(answer), tokenstring))
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
