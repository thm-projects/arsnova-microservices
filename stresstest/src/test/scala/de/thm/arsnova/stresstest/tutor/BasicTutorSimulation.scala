package de.thm.arsnova.stresstest.tutor

import java.util.Calendar
import java.util.UUID
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

import de.thm.arsnova.shared.entities._

object BasicTutorSimulation extends TutorScenario {
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._
  import de.thm.arsnova.shared.mappings.ContentJsonProtocol._

  val mcAnswerOptions = Seq(
    AnswerOption(None, None, false, "12", -10),
    AnswerOption(None, None, true, "13", 10),
    AnswerOption(None, None, false, "14", -10),
    AnswerOption(None, None, true, "thirteen", 10)
  )
  val newMCQuestion = Content(None, UUID.randomUUID, "new Question Subject", "This is an MC question for stress testing",
    "preparation", "mc", Some("This is the hint!"), Some("The answer is 13"), true, false, true, true, false, None, Some(mcAnswerOptions))

  val scn = scenario("Basic Tutor").exec(
    createSession(basicNewSession),
    createQuestion(newMCQuestion, "mc")
  )
}