package de.thm.arsnova.stresstest

import java.util.Calendar
import java.util.UUID
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import de.thm.arsnova.shared.entities._

class Stresstest extends Simulation {
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  val httpProtocol = http
    .baseURL("http://localhost:9000")
    .inferHtmlResources(BlackList(""".*\.css""", """.*\.js""", """.*\.ico"""), WhiteList())
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .doNotTrackHeader("1")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0")

  val now = Calendar.getInstance.getTime.toString
  val newSession = Session(None, "12312312", UUID.fromString("b055f5d8-1f8c-11e7-93ae-92361f002671"), "A new Session", "ans", now, now, true, false, false)

  val createSession = exec(http("Tutor creates session")
    .post("/session/")
    .header("Content-Type", "application/json")
    .body(StringBody(newSession.toJson.toString)).asJSON)

  val scn = scenario("Basic Tutor").exec(
    createSession
  )

  setUp(
    scn.inject(rampUsers(1000) over (5 seconds))
  ).protocols(httpProtocol)
}