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
  import de.thm.arsnova.shared.mappings.RoomJsonProtocol._

  val now = Calendar.getInstance.getTime.toString
  val basicNewRoom = Room(None, None, UUID.randomUUID, "A new room", "ans", now, now, true, false, false)

  def createRoom(room: de.thm.arsnova.shared.entities.Room) = exec(
    http("Tutor creates room")
      .post("/room/")
      .header("Content-Type", "application/json")
      .body(StringBody(room.toJson.toString)).asJSON
      .check(jsonPath("$").saveAs("roomId"))
  ).pause(4)

  def createQuestion(question: Content, name: String) = exec(
    http(s"Tutor creates ${name} question")
      .post("/room/${roomId}/question")
      .header("Content-Type", "application/json")
      .body(StringBody(question.toJson.toString)).asJSON
  ).pause(4)
}