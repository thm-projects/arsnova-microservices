package de.thm.arsnova.gateway.api

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import spray.json._

import de.thm.arsnova.gateway.api.BaseApi
import de.thm.arsnova.shared.entities.FreetextAnswer
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands._

trait FreetextAnswerApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.FreetextAnswerJsonProtocol._

  val freetextAnswerApi = pathPrefix("session") {
    pathPrefix(JavaUUID) { sessionId =>
      pathPrefix("question") {
        pathPrefix(JavaUUID) { questionId =>
          pathPrefix("freetextanswer") {
            pathPrefix(JavaUUID) { answerId =>
              get {
                complete {
                  (remoteCommander ? GetFreetextAnswer(answerId))
                    .mapTo[FreetextAnswer].map(_.toJson)
                }
              }
            }
            get {
              complete {
                (remoteCommander ? GetFreetextAnswersByQuestionId(questionId))
                  .mapTo[Seq[FreetextAnswer]].map(_.toJson)
              }
            } ~
            post {
              entity(as[FreetextAnswer]) { answer =>
                complete {
                  (remoteCommander ? CreateFreetextAnswer(answer))
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
