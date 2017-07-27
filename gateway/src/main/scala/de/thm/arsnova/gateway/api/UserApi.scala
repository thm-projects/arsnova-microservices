package de.thm.arsnova.gateway.api

import java.util.UUID

import akka.actor.Props
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.routing.RandomPool
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.gateway.Context._
import de.thm.arsnova.authservice.UserActor
import spray.json._
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.entities.{User, Session}
import de.thm.arsnova.shared.Exceptions._

trait UserApi extends BaseApi {
  import de.thm.arsnova.shared.mappings.UserJsonProtocol._
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  ClusterSharding(system).startProxy(
    typeName = UserActor.shardName,
    role = Some("auth"),
    extractEntityId = UserActor.idExtractor,
    extractShardId = UserActor.shardResolver)

  val userRegion = ClusterSharding(system).shardRegion(UserActor.shardName)

  val userApi = pathPrefix("user") {
    pathPrefix(JavaUUID) { userId =>
      pathEndOrSingleSlash {
        get {
          complete {
            (userRegion ? GetUser(userId))
              .mapTo[Option[User]].map {
              case Some(user) => user.toJson
              case None => ResourceNotFound("user").toJson
            }
          }
        }
      } ~
      pathPrefix("sessions") {
        get {
          complete {
            (userRegion ? GetUserSessions(userId))
              .mapTo[Seq[Session]].map.(_.toJson)
          }
        }
      }
    }
  }
}
