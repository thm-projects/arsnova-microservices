package de.thm.arsnova.sessionservice

import java.util.UUID

import scala.util.{Failure, Success}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe
import de.thm.arsnova.shared.commands.SessionCommands._
import de.thm.arsnova.shared.entities.Session

import scala.concurrent.ExecutionContext

class DispatcherActor extends Actor {
  implicit val ex: ExecutionContext = context.system.dispatcher
  def receive = {
    case GetSession(id) => ((ret: ActorRef) => {
      SessionRepository.findById(id) pipeTo ret
    }) (sender)
    case GetSessionByKeyword(keyword) => ((ret: ActorRef) => {
      SessionRepository.findByKeyword(keyword) pipeTo ret
    }) (sender)
    case CreateSession(session) => ((ret: ActorRef) => {
      SessionRepository.create(session) pipeTo ret
    }) (sender)
  }
}
