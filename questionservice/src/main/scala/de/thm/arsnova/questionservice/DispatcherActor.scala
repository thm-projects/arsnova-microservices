package de.thm.arsnova.questionservice

import java.util.UUID

import scala.util.{Failure, Success}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe
import de.thm.arsnova.shared.commands.QuestionCommands._
import de.thm.arsnova.shared.entities.Question

import scala.concurrent.ExecutionContext

class DispatcherActor extends Actor {
  implicit val ex: ExecutionContext = context.system.dispatcher
  def receive = {
    case GetQuestion(id) => ((ret: ActorRef) => {
      QuestionRepository.findById(id) pipeTo ret
    }) (sender)
    case GetQuestionsBySessionId(id) => ((ret: ActorRef) => {
      QuestionRepository.findBySessionId(id) pipeTo ret
    }) (sender)
  }
}
