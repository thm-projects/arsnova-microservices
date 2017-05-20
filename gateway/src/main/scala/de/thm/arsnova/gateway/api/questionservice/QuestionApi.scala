package de.thm.arsnova.gateway.api.questionservice

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.gateway.api.BaseApi
import de.thm.arsnova.shared.entities.Question
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.servicecommands.QuestionCommands._

trait QuestionApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.QuestionJsonProtocol._

  val questionApi = pathPrefix("session") {
    optionalHeaderValueByName("X-Session-Token") { tokenstring =>
      pathPrefix(JavaUUID) { sessionId =>
        pathPrefix("question") {
          pathPrefix(JavaUUID) { questionId =>
            get {
              complete {
                (remoteCommander ? CommandWithToken(GetQuestion(questionId), tokenstring))
                  .mapTo[Question].map(_.toJson)
              }
            }
          } ~
          get {
            complete {
              (remoteCommander ? CommandWithToken(GetQuestionsBySessionId(sessionId), tokenstring))
                .mapTo[Seq[Question]].map(_.toJson)
            }
          } ~
          get {
            parameters("variant") { variant =>
              complete {
                (remoteCommander ? CommandWithToken(GetQuestionsBySessionIdAndVariant(sessionId, variant), tokenstring))
                  .mapTo[Seq[Question]].map(_.toJson)
              }
            }
          } ~
          post {
            entity(as[Question]) { question =>
              complete {
                (remoteCommander ? CommandWithToken(CreateQuestion(question.copy(sessionId = sessionId)), tokenstring))
                  .mapTo[UUID].map(_.toJson)
              }
            }
          }
        }
      }
    }
  }
}
