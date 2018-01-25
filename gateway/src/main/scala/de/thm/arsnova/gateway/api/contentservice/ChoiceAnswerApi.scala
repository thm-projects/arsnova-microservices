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
import de.thm.arsnova.shared.entities.{ChoiceAnswer, RoundTransition, User}
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands._
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.AuthCommands.AuthenticateUser

trait ChoiceAnswerApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.ChoiceAnswerJsonProtocol._
  import de.thm.arsnova.shared.mappings.RoundTransitionJsonProtocol._

  val choiceAnswerApi = pathPrefix("room") {
    pathPrefix(JavaUUID) { roomId =>
      pathPrefix("content") {
        pathPrefix(JavaUUID) { contentId =>
          pathPrefix("choiceanswer") {
            pathPrefix(JavaUUID) { answerId =>
              get {
                complete {
                  (answerListRegion ? GetChoiceAnswer(contentId, answerId))
                    .mapTo[Option[ChoiceAnswer]]
                }
              } ~
              get {
                parameters("roundA".as[Int], "roundB".as[Int]) { (roundA, roundB) =>
                  complete {
                    (answerListRegion ? GetTransitions(contentId, roundA, roundB))
                      .mapTo[Try[Seq[RoundTransition]]]
                  }
                }
              } ~
              delete {
                headerValueByName("X-Session-Token") { token =>
                  complete {
                    (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                      case Success(uId) => {
                        (answerListRegion ? DeleteChoiceAnswer(contentId, roomId, answerId, uId))
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
                (answerListRegion ? GetChoiceAnswers(contentId))
                  .mapTo[Seq[ChoiceAnswer]]
              }
            } ~
            post {
              headerValueByName("X-Session-Token") { token =>
                entity(as[ChoiceAnswer]) { answer =>
                  complete {
                    (authClient ? AuthenticateUser(token)).mapTo[Try[UUID]] map {
                      case Success(uId) => {
                        (answerListRegion ? CreateChoiceAnswer(contentId, roomId, answer, uId))
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
