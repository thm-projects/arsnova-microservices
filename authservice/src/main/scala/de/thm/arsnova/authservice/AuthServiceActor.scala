package de.thm.arsnova.authservice

import java.util.UUID

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import de.thm.arsnova.authservice.repositories._
import de.thm.arsnova.shared.servicecommands.AuthCommands._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.cluster.Cluster
import akka.pattern.pipe
import de.thm.arsnova.shared.Exceptions.{InvalidToken, NoUserException}

class AuthServiceActor extends Actor {
  implicit val ex: ExecutionContext = context.system.dispatcher

  def receive = {
    case LoginUser(username, password) => ((ret: ActorRef) => {
      UserRepository.verifyLogin(username, password).map {
        case Some(uuid) => TokenRepository.create(uuid) pipeTo ret
        case None => Future { "not found" } pipeTo ret
      }
    }) (sender)
    case GetUserFromTokenString(tokenstring) => ((ret: ActorRef) => {
      UserRepository.getUserByTokenString(tokenstring) map {
        case Some(u) => ret ! Success(u)
        case None => ret ! Failure(InvalidToken(tokenstring))
      }
    }) (sender)

    case CheckTokenString(tokenstring) => ((ret: ActorRef) => {
      UserRepository.checkTokenString(tokenstring) pipeTo ret
    }) (sender)
  }
}
