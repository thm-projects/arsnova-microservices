package de.thm.arsnova.commentservice

import java.util.UUID

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe

import de.thm.arsnova.shared.servicecommands.CommentCommands._
import de.thm.arsnova.shared.entities.Comment
import de.thm.arsnova.shared.management.CommandPackage

class DispatcherActor extends Actor {
  implicit val ex: ExecutionContext = context.system.dispatcher

  def receive = {
    case CommandPackage(command, user, returnRef) => {
      command match {
        case GetComment(id) => {
          CommentRepository.findById(id) pipeTo returnRef
        }
        case GetCommentBySessionId(id) => {
          CommentRepository.findBySessionId(id) pipeTo returnRef
        }
        case CreateComment(session) => {
          CommentRepository.create(session) pipeTo returnRef
        }
      }
    }
  }
}
