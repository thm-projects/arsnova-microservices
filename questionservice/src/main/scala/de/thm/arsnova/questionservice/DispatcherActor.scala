package de.thm.arsnova.questionservice

import java.util.UUID

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.pipe

import de.thm.arsnova.questionservice.repositories._
import de.thm.arsnova.shared.entities.{ChoiceAnswer, FreetextAnswer, Question}
import de.thm.arsnova.shared.management.CommandPackage
import de.thm.arsnova.shared.servicecommands.QuestionCommands._
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands._
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands._

class DispatcherActor extends Actor {
  implicit val ex: ExecutionContext = context.system.dispatcher

  def receive = {
    case CommandPackage(command, user, returnRef) => {

      command match {
        case GetQuestion(id) => {
          QuestionRepository.findById(id) pipeTo returnRef
        }
        case GetQuestionsBySessionId(id) => {
          QuestionRepository.findBySessionId(id) pipeTo returnRef
        }
        case CreateQuestion(question) => {
          QuestionRepository.create(question) pipeTo returnRef
        }

        case GetChoiceAnswer(id) => {
          ChoiceAnswerRepository.findById(id) pipeTo returnRef
        }
        case GetChoiceAnswersByQuestionId(id) => {
          ChoiceAnswerRepository.findByQuestionId(id) pipeTo returnRef
        }
        case CreateChoiceAnswer(answer) => {
          ChoiceAnswerRepository.create(answer) pipeTo returnRef
        }

        case GetFreetextAnswer(id) => {
          FreetextAnswerRepository.findById(id) pipeTo returnRef
        }
        case GetFreetextAnswersByQuestionId(id) => {
          FreetextAnswerRepository.findByQuestionId(id) pipeTo returnRef
        }
        case CreateFreetextAnswer(answer) => {
          FreetextAnswerRepository.create(answer) pipeTo returnRef
        }
      }
    }
  }
}
