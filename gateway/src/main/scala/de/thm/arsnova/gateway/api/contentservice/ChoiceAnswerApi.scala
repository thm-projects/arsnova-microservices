package de.thm.arsnova.gateway.api.contentservice

import java.util.UUID

import akka.actor.Props

import scala.util.Try
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import de.thm.arsnova.gateway.AuthServiceClientActor
import de.thm.arsnova.gateway.Context._
import spray.json._
import de.thm.arsnova.gateway.api.BaseApi
import de.thm.arsnova.shared.entities.{ChoiceAnswer, User}
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands._
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.AuthCommands.AuthenticateUser

trait ChoiceAnswerApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.ChoiceAnswerJsonProtocol._

  val authClient = system.actorOf(Props[AuthServiceClientActor], name = "authClient")

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
                    (authClient ? AuthenticateUser).mapTo[Try[UUID]] map {
                      case Success(uId) => {
                        (choiceAnswerListRegion ? DeleteChoiceAnswer(sessionId, questionId, answerId, uId))
                          .mapTo[Try[ChoiceAnswer]]
                      }
                      case Failure(t) => Future.failed(t)
                    }
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
                    (authClient ? AuthenticateUser).mapTo[Try[UUID]] map {
                      case Success(uId) => {
                        (choiceAnswerListRegion ? CreateChoiceAnswer(sessionId, questionId, answer, uId))
                          .mapTo[Try[ChoiceAnswer]]
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
