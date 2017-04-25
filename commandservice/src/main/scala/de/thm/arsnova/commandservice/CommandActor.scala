package de.thm.arsnova.commandservice

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import de.thm.arsnova.commandservice.repositories._
import de.thm.arsnova.shared.commands.AuthCommands._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe

import de.thm.arsnova.shared.commands._

class CommandActor extends Actor {
  def receive = {
    case c: Command => {

    }
  }
}