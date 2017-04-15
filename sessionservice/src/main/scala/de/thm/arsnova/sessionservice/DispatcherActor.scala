package de.thm.arsnova.sessionservice

import java.util.UUID

import akka.actor.Actor
import akka.actor.ActorRef
import de.thm.arsnova.shared.commands.SessionCommands._
import de.thm.arsnova.shared.entities.Session

class DispatcherActor extends Actor {
  def receive = {
    case GetSession(id) => ((ret: ActorRef) => {
      ret ! Session(None, "wololo", UUID.randomUUID, "title", "sN", "asdf", "asdf", true, false, true)
    }) (sender)
  }
}
