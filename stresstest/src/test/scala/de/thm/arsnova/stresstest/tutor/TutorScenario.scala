package de.thm.arsnova.stresstest.tutor

import java.util.UUID
import java.util.Calendar
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import de.thm.arsnova.shared.entities._

trait TutorScenario {
  import de.thm.arsnova.shared.mappings.ContentJsonProtocol._
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  val now = Calendar.getInstance.getTime.toString
  val basicNewSession = Session(None, None, UUID.randomUUID, "A new Session", "ans", now, now, true, false, false)

  def createSession(session: de.thm.arsnova.shared.entities.Session) = exec(
    http("Tutor creates session")
      .post("/session/")
      .header("Content-Type", "application/json")
      .body(StringBody(session.toJson.toString)).asJSON
      .check(jsonPath("$").saveAs("sessionId"))
  ).pause(4)

  def createQuestion(question: Content, name: String) = exec(
    http(s"Tutor creates ${name} question")
      .post("/session/${sessionId}/question")
      .header("Content-Type", "application/json")
      .body(StringBody(question.toJson.toString)).asJSON
  ).pause(4)
}