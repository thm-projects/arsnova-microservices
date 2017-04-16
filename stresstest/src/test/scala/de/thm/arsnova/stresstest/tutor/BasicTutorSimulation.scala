package de.thm.arsnova.stresstest.tutor

import java.util.Calendar
import java.util.UUID
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import de.thm.arsnova.shared.entities._

object BasicTutorSimulation {
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  val now = Calendar.getInstance.getTime.toString
  val newSession = Session(None, "12312312", UUID.fromString("b055f5d8-1f8c-11e7-93ae-92361f002671"), "A new Session", "ans", now, now, true, false, false)

  val createSession = exec(http("Tutor creates session")
    .post("/session/")
    .header("Content-Type", "application/json")
    .body(StringBody(newSession.toJson.toString)).asJSON)
}