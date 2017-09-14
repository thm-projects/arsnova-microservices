package de.thm.arsnova.authservice

import java.util.UUID

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import de.thm.arsnova.authservice.repositories._
import de.thm.arsnova.shared.servicecommands.AuthCommands._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.cluster.Cluster
import akka.pattern.{ask, pipe}
import akka.cluster.sharding.ClusterSharding
import akka.util.Timeout
import de.thm.arsnova.shared.Exceptions.{InvalidToken, NoUserException}
import de.thm.arsnova.shared.shards.UserShard
import de.thm.arsnova.shared.servicecommands.UserCommands._

class AuthServiceActor extends Actor {
  import Context.system

  implicit val ex: ExecutionContext = context.system.dispatcher
  implicit val timeout: Timeout = 5.seconds
  val userRegion = ClusterSharding(system).shardRegion(UserShard.shardName)

  def receive = {
    case LoginUser(username, password) => ((ret: ActorRef) => {
      UserRepository.verifyLogin(username, password).map {
        case Some(uuid) => TokenRepository.create(uuid) pipeTo ret
        case None => Future { "not found" } pipeTo ret
      }
    }) (sender)
    case AuthenticateUser(token) => {
      TokenRepository.getByToken(token) pipeTo sender()
    }

    case CheckTokenString(tokenstring) => ((ret: ActorRef) => {
      UserRepository.checkTokenString(tokenstring) pipeTo ret
    }) (sender)
  }
}
