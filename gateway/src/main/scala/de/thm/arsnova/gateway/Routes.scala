package de.thm.arsnova.gateway

import akka.http.scaladsl.server.Directives._
import de.thm.arsnova.gateway.api._

trait Routes extends ApiErrorHandler
  with SessionServiceApi
  with QuestionServiceApi
  with CommentServiceApi
  with AuthApi {
  val routes = {
    sessionApi ~
    questionServiceApi ~
    commentServiceApi ~
    authApi
  }
}
