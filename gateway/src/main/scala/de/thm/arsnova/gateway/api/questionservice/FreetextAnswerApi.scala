package de.thm.arsnova.gateway.api

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.shared.entities.FreetextAnswer
import de.thm.arsnova.shared.commands.FreetextAnswerCommands._

trait FreetextAnswerApi {
  import de.thm.arsnova.gateway.Context._

  import de.thm.arsnova.shared.mappings.FreetextAnswerJsonProtocol._

  implicit val timeoutFreetextAnswer = Timeout(5.seconds)
  val remoteFreetextAnswer = system.actorSelection("akka://QuestionService@127.0.0.1:9002/user/dispatcher")

  val freetextAnswerApi = pathPrefix("session") {
    pathPrefix(JavaUUID) { sessionId =>
      pathPrefix("question") {
        pathPrefix(JavaUUID) { questionId =>
          pathPrefix("freetextanswer") {
            pathPrefix(JavaUUID) { answerId =>
              get {
                complete {
                  (remoteFreetextAnswer ? GetFreetextAnswer(answerId))
                    .mapTo[FreetextAnswer].map(_.toJson)
                }
              }
            }
            get {
              complete {
                (remoteFreetextAnswer ? GetFreetextAnswersByQuestionId(questionId))
                  .mapTo[Seq[FreetextAnswer]].map(_.toJson)
              }
            } ~
            post {
              entity(as[FreetextAnswer]) { answer =>
                complete {
                  (remoteFreetextAnswer ? CreateFreetextAnswer(answer))
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
