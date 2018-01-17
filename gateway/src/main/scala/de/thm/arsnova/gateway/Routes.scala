package de.thm.arsnova.gateway

import akka.http.scaladsl.server.Directives._
import de.thm.arsnova.gateway.api._

trait Routes extends ApiErrorHandler
  with RoomServiceApi
  with RoomExportServiceApi
  with ContentServiceApi
  with CommentServiceApi
  with AuthApi
  with UserApi {
  val routes = {
    roomApi ~
    roomExportApi ~
    contentServiceApi ~
    commentServiceApi ~
    authApi ~
    userApi
  }
}
