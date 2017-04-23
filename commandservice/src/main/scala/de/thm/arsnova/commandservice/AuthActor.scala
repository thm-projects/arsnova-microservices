import java.util.UUID

import scala.util.{Failure, Success}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe

import de.thm.arsnova.commandservice.repositories._
import de.thm.arsnova.shared.commands.AuthCommands._

class AuthActor extends Actor {
  def receive = {
    case LoginUser(username, password) => ((ret: ActorRef) => {
       UserRepository.verifyLogin(username, password).map {
         case Success(uuid) => TokenRepository.create(uuid) pipeTo sender
         case Failure(f) => Future{ f } pipeTo sender
       }
    }) (sender)
  }
}
