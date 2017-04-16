package de.thm.arsnova.stresstest

import java.util.Calendar
import java.util.UUID
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

import de.thm.arsnova.stresstest.tutor._
import de.thm.arsnova.stresstest.auditor._
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

  val tutorScn = scenario("Basic Tutor").exec(
    BasicTutorSimulation.createSession
  )

  setUp(
    tutorScn.inject(rampUsers(100) over (5 seconds))
  ).protocols(httpProtocol)
}