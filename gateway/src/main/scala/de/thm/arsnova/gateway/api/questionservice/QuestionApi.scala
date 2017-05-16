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
import de.thm.arsnova.shared.entities.Question
import de.thm.arsnova.shared.servicecommands.QuestionCommands._

trait QuestionApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.QuestionJsonProtocol._

  val questionApi = pathPrefix("session") {
    pathPrefix(JavaUUID) { sessionId =>
      pathPrefix("question") {
        pathPrefix(JavaUUID) { questionId =>
          get {
            complete {
              (remoteCommander ? GetQuestion(questionId))
                .mapTo[Question].map(_.toJson)
            }
          }
        } ~
        get {
          complete {
            (remoteCommander ? GetQuestionsBySessionId(sessionId))
              .mapTo[Seq[Question]].map(_.toJson)
          }
        } ~
        get {
          parameters("variant") { variant =>
            complete {
              (remoteCommander ? GetQuestionsBySessionIdAndVariant(sessionId, variant))
                .mapTo[Seq[Question]].map(_.toJson)
            }
          }
        } ~
        post {
          entity(as[Question]) { question =>
            complete {
              (remoteCommander ? CreateQuestion(question.copy(sessionId = sessionId)))
                .mapTo[UUID].map(_.toJson)
            }
          }
        }
      }
    }
  }
}
