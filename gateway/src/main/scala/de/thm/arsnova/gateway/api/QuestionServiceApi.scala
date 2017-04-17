package de.thm.arsnova.gateway.api

import akka.http.scaladsl.server.Directives._

import de.thm.arsnova.gateway.api.questionservice._

trait QuestionServiceApi
  extends QuestionApi
  with ChoiceAnswerApi
  with FreetextAnswerApi {

  val questionServiceApi =
    questionApi ~
    choiceAnswerApi ~
    freetextAnswerApi
}
