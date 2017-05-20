package de.thm.arsnova.sessionservice

import java.util.UUID

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe

import de.thm.arsnova.shared.servicecommands.SessionCommands._
import de.thm.arsnova.shared.management.CommandPackage
import de.thm.arsnova.shared.entities.Session


class DispatcherActor extends Actor {
  implicit val ex: ExecutionContext = context.system.dispatcher

  def receive = {
    case CommandPackage(command, user, returnRef) => {
      command match {
        case GetSession(id) => {
          SessionRepository.findById(id) pipeTo returnRef
        }
        case GetSessionByKeyword(keyword) => {
          SessionRepository.findByKeyword(keyword) pipeTo returnRef
        }
        case CreateSession(session) => {
          SessionRepository.create(session) pipeTo returnRef
        }
      }
    }
  }
}
