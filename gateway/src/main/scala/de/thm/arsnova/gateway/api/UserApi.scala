package de.thm.arsnova.gateway.api

import java.util.UUID

import akka.actor.Props
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.routing.RandomPool
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.gateway.Context._
import spray.json._
import de.thm.arsnova.roomservice.UserActor
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.entities.{Room, User}
import de.thm.arsnova.shared.Exceptions._

trait UserApi extends BaseApi {
  import de.thm.arsnova.gateway.Context._
  import de.thm.arsnova.shared.mappings.RoomJsonProtocol._
  import de.thm.arsnova.shared.mappings.UserJsonProtocol._

  val userApi = pathPrefix("user") {
    pathPrefix(JavaUUID) { userId =>
      pathEndOrSingleSlash {
        get {
          complete {
            (userRegion ? GetUser(userId))
              .mapTo[Try[User]]
          }
        }
      }
    } ~
    post {
      entity(as[User]) { user =>
        complete {
          val newId = UUID.randomUUID()
          val userWithId = user.copy(id = Some(newId))
          (userRegion ? CreateUser(newId, userWithId))
            .mapTo[Try[User]]
        }
      }
    }
  }
}
