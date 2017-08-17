package de.thm.arsnova.contentservice

import scala.util.{Failure, Success, Try}
import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import akka.pattern.ask
import de.thm.arsnova.contentservice.repositories.ChoiceAnswerRepository
import de.thm.arsnova.shared.entities.{ChoiceAnswer, User}
import de.thm.arsnova.shared.servicecommands.AuthCommands.GetUserFromTokenString
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands._

import scala.concurrent.Future
import scala.util.Try

object ChoiceAnswerActor {
  def props(authRouter: ActorRef): Props =
    Props(new ChoiceAnswerActor(authRouter: ActorRef))
}

class ChoiceAnswerActor(authRouter: ActorRef) extends Actor {

  def tokenToUser(tokenstring: String): Future[Try[User]] = {
    (authRouter ? GetUserFromTokenString(tokenstring)).mapTo[Try[User]]
  }
  override def receive: Receive = {
    case CreateChoiceAnswer(sessionId, questionId, answer, token) => ((ret: ActorRef) => {
      tokenToUser(token) map {
        case Success(user) => {
          val awu = answer.copy(userId = user.id.get)
          ChoiceAnswerRepository.create(awu) map { aRet =>
            ret ! Success(aRet)
          }
        }
        case Failure(t) => {
          ret ! Failure(t)
        }
      }
    }) (sender)
  }
}
