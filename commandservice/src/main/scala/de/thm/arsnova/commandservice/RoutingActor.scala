package de.thm.arsnova.commandservice

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.Actor
import akka.actor.ActorRef

import de.thm.arsnova.shared.servicecommands.AuthCommands.AuthCommand
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands.ChoiceAnswerCommand
import de.thm.arsnova.shared.servicecommands.CommentCommands.CommentCommand
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands.FreetextAnswerCommand
import de.thm.arsnova.shared.servicecommands.QuestionCommands.QuestionCommand
import de.thm.arsnova.shared.servicecommands.SessionCommands.SessionCommand
import de.thm.arsnova.shared.management.CommandPackage
import de.thm.arsnova.shared.management.RegistryCommands.RegisterService

class RoutingActor extends Actor {
  val cache = collection.mutable.Map[String, ActorRef]()

  def receive = {
    // registry commands
    case RegisterService(serviceType, remote) =>
      cache(serviceType) = remote

    // service commands
    case p: CommandPackage => {
      p.command match {
        case c: AuthCommand => {
          println("AuthCommand incoming")
          cache.get("AuthService") match {
            case Some(ref) => ref ! p
            case None => println("no service for this command")
          }
        }

        case c: SessionCommand => {
          println("SessionCommand incoming")
          cache.get("SessionService") match {
            case Some(ref) => ref ! p
            case None => println("no service for this command")
          }
        }

        case c: QuestionCommand => {
          println("QuestionCommand incoming")
          cache.get("QuestionService") match {
            case Some(ref) => ref ! p
            case None => println("no service for this command")
          }
        }
        case c: ChoiceAnswerCommand => {
          println("ChoiceAnswerCommand incoming")
          cache.get("QuestionService") match {
            case Some(ref) => ref ! p
            case None => println("no service for this command")
          }
        }
        case c: FreetextAnswerCommand => {
          println("FreetextAnswerCommand incoming")
          cache.get("QuestionService") match {
            case Some(ref) => ref ! p
            case None => println("no service for this command")
          }
        }

        case c: CommentCommand => {
          println("CommentCommand incoming")
          cache.get("CommentService") match {
            case Some(ref) => ref ! p
            case None => println("no service for this command")
          }
        }
      }
    }
  }
}
