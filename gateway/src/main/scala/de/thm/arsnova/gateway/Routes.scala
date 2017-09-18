package de.thm.arsnova.gateway

import akka.http.scaladsl.server.Directives._
import de.thm.arsnova.gateway.api._

trait Routes extends ApiErrorHandler
  with RoomServiceApi
  with ContentServiceApi
  with CommentServiceApi
  with AuthApi
  with UserApi {
  val routes = {
    roomApi ~
    contentServiceApi ~
    commentServiceApi ~
    authApi ~
    userApi
  }
}
