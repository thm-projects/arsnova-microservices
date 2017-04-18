package de.thm.arsnova.sessionservice

import java.util.UUID

import scala.util.{Failure, Success}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe
import de.thm.arsnova.shared.commands.CommentCommands._
import de.thm.arsnova.shared.entities.Comment

import scala.concurrent.ExecutionContext

class DispatcherActor extends Actor {
  implicit val ex: ExecutionContext = context.system.dispatcher
  def receive = {
    case GetComment(id) => ((ret: ActorRef) => {
      CommentRepository.findById(id) pipeTo ret
    }) (sender)
    case GetCommentBySessionId(id) => ((ret: ActorRef) => {
      CommentRepository.findBySessionId(id) pipeTo ret
    }) (sender)
    case CreateComment(session) => ((ret: ActorRef) => {
      CommentRepository.create(session) pipeTo ret
    }) (sender)
  }
}
