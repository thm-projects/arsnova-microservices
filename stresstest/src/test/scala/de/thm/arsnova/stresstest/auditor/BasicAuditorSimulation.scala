package de.thm.arsnova.stresstest.auditor

import java.util.UUID
import java.util.Calendar
import io.gatling.core.Predef._ // 2
import io.gatling.http.Predef._
import scala.concurrent.duration._
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import de.thm.arsnova.shared.entities._

object BasicAuditorSimulation {
  import de.thm.arsnova.shared.mappings.ChoiceAnswerJsonProtocol._
  import de.thm.arsnova.shared.mappings.CommentJsonProtocol._

  val now = Calendar.getInstance.getTime.toString

  val joinSession = exec(http("Auditor joins session")
    .get("/session/a1927004-b489-4b85-a90d-17ecf5996d57"))

  /*val getAllPrepQuestions = exec(http("Auditor gets all preparation questions")
    .get("/session/42664be0-35d1-45c7-a87d-d2ed9cc9cad7/question/")
  )

  val newAnswer = ChoiceAnswer(None, UUID.fromString("fa705322-16fa-4987-99a6-2abe767ce832"),
    UUID.fromString("42664be0-35d1-45c7-a87d-d2ed9cc9cad7"), UUID.fromString("f4ba953e-1a99-43aa-95a4-f0f3bfbe26d4"))

  val answerToMCQuestion = exec(http("Auditor answers mc question")
    .post("/session/42664be0-35d1-45c7-a87d-d2ed9cc9cad7/question/fa705322-16fa-4987-99a6-2abe767ce832/choiceanswer")
    .header("Content-Type", "application/json")
    .body(StringBody(newAnswer.toJson.toString)).asJSON)

  val newComment = Comment(None, UUID.randomUUID, UUID.fromString("42664be0-35d1-45c7-a87d-d2ed9cc9cad7"),
    false, "this the subject", "i have a question and i dare not ask.", now)

  val postComment = exec(http("Auditor posts a comment")
    .post("/session/42664be0-35d1-45c7-a87d-d2ed9cc9cad7/comment")
    .header("Content-Type", "application/json")
    .body(StringBody(newComment.toJson.toString)).asJSON
  )

  val scn = scenario("Test").exec(
    joinSession.pause(3),
    getAllPrepQuestions.pause(3),
    answerToMCQuestion.pause(3),
    postComment
  )*/
  val scn = scenario("Test").exec(joinSession)
}