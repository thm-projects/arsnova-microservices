package de.thm.arsnova.commandservice

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.{pipe, ask}

import de.thm.arsnova.shared.servicecommands.AuthCommands._
import de.thm.arsnova.shared.servicecommands._
import de.thm.arsnova.shared.management.CommandPackage
import de.thm.arsnova.shared.entities.User

class CommandActor extends Actor {
  import Context.{timeout, executionContext}

  val router = context.actorSelection("akka://CommandService@127.0.0.1:8880/user/router")

  def tokenToUser(tokenstring: Option[String]): Future[Option[User]] = tokenstring match {
    case Some(t) => (router ? GetUserFromTokenString(t)).mapTo[Option[User]]
    case None => Future { None }
  }

  def receive = {
    // auth commands don't have 'extra' tokens
    // if tokens are needed, they are capsuled inside the command
    case a: AuthCommand => ((ret: ActorRef) => {
      router ! CommandPackage(a, None, ret)
    }) (sender)

    // normal command which may or may not have a token (Option[])
    case CommandWithToken(command, token) => ((ret: ActorRef) => {
      tokenToUser(token).map { optionalUser =>
        router ! CommandPackage(command, optionalUser, ret)
      }
    }) (sender)
  }
}