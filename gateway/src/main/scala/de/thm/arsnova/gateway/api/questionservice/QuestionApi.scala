package de.thm.arsnova.gateway.api.questionservice

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.shared.entities.Question
import de.thm.arsnova.shared.commands.QuestionCommands._

trait QuestionApi {
  import de.thm.arsnova.gateway.Context._

  import de.thm.arsnova.shared.mappings.QuestionJsonProtocol._

  implicit val timeoutQuestion = Timeout(10.seconds)
  val remoteQuestion = system.actorSelection("akka://QuestionService@127.0.0.1:9002/user/dispatcher")

  val questionApi = pathPrefix("session") {
    pathPrefix(JavaUUID) { sessionId =>
      pathPrefix("question") {
        pathPrefix(JavaUUID) { questionId =>
          get {
            complete {
              (remoteQuestion ? GetQuestion(questionId))
                .mapTo[Question].map(_.toJson)
            }
          }
        } ~
        get {
          complete {
            (remoteQuestion ? GetQuestionsBySessionId(sessionId))
              .mapTo[Seq[Question]].map(_.toJson)
          }
        } ~
        get {
          parameters("variant") { variant =>
            complete {
              (remoteQuestion ? GetQuestionsBySessionIdAndVariant(sessionId, variant))
                .mapTo[Seq[Question]].map(_.toJson)
            }
          }
        } ~
        post {
          entity(as[Question]) { question =>
            complete {
              (remoteQuestion ? CreateQuestion(question.copy(sessionId = sessionId)))
                .mapTo[UUID].map(_.toJson)
            }
          }
        }
      }
    }
  }
}
