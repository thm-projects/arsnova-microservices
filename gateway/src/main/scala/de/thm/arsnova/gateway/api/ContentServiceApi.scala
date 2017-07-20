package de.thm.arsnova.gateway.api

import akka.http.scaladsl.server.Directives._

import de.thm.arsnova.gateway.api.contentservice._

trait ContentServiceApi
  extends ContentServiceApi
  with ChoiceAnswerApi
  with FreetextAnswerApi {

  val questionServiceApi =
    contentServiceApi ~
    choiceAnswerApi ~
    freetextAnswerApi
}
