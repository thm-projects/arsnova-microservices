package de.thm.arsnova.gateway.api

import akka.http.scaladsl.server.Directives._

import de.thm.arsnova.gateway.api.contentservice._

trait ContentServiceApi
  extends ContentApi
  with ChoiceAnswerApi
  with FreetextAnswerApi {

  val contentServiceApi =
    contentApi ~
    choiceAnswerApi ~
    freetextAnswerApi
}
