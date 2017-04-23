package de.thm.arsnova.commandservice

import java.util.UUID
import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import de.thm.arsnova.commandservice.repositories._
import de.thm.arsnova.shared.commands.AuthCommands._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe

class AuthActor extends Actor {
  implicit val ex: ExecutionContext = context.system.dispatcher

  def receive = {
    case LoginUser(username, password) => ((ret: ActorRef) => {
       UserRepository.verifyLogin(username, password).map {
         case Some(uuid) => TokenRepository.create(uuid) pipeTo ret
         case None => Future { "not found" } pipeTo ret
       }
    }) (sender)
    case CreateUser(user) => ((ret: ActorRef) => {
      UserRepository.create(user) pipeTo ret
    }) (sender)

    case CheckTokenString(tokenstring) => ((ret: ActorRef) => {
      UserRepository.checkTokenString(tokenstring) pipeTo ret
    }) (sender)
  }
}
