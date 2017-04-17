package de.thm.arsnova.questionservice

import java.util.UUID

import scala.util.{Failure, Success}
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe
import scala.concurrent.ExecutionContext

import de.thm.arsnova.questionservice.repositories._
import de.thm.arsnova.shared.entities.{Question, ChoiceAnswer, FreetextAnswer}
import de.thm.arsnova.shared.commands.QuestionCommands._
import de.thm.arsnova.shared.commands.ChoiceAnswerCommands._
import de.thm.arsnova.shared.commands.FreetextAnswerCommands._

class DispatcherActor extends Actor {
  implicit val ex: ExecutionContext = context.system.dispatcher
  def receive = {
    case GetQuestion(id) => ((ret: ActorRef) => {
      QuestionRepository.findById(id) pipeTo ret
    }) (sender)
    case GetQuestionsBySessionId(id) => ((ret: ActorRef) => {
      QuestionRepository.findBySessionId(id) pipeTo ret
    }) (sender)
    case CreateQuestion(question) => ((ret: ActorRef) => {
      QuestionRepository.create(question) pipeTo ret
    }) (sender)

    case GetChoiceAnswer(id) => ((ret: ActorRef) => {
      ChoiceAnswerRepository.findById(id) pipeTo ret
    }) (sender)
    case GetChoiceAnswersByQuestionId(id) => ((ret: ActorRef) => {
      ChoiceAnswerRepository.findByQuestionId(id) pipeTo ret
    }) (sender)
    case CreateChoiceAnswer(answer) => ((ret: ActorRef) => {
      ChoiceAnswerRepository.create(answer) pipeTo ret
    }) (sender)

    case GetFreetextAnswer(id) => ((ret: ActorRef) => {
      FreetextAnswerRepository.findById(id) pipeTo ret
    }) (sender)
    case GetFreetextAnswersByQuestionId(id) => ((ret: ActorRef) => {
      FreetextAnswerRepository.findByQuestionId(id) pipeTo ret
    }) (sender)
    case CreateFreetextAnswer(answer) => ((ret: ActorRef) => {
      FreetextAnswerRepository.create(answer) pipeTo ret
    }) (sender)
  }
}
