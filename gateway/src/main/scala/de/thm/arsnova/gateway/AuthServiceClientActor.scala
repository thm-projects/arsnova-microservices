package de.thm.arsnova.gateway

import java.util.UUID

import scala.util.{Try, Success, Failure}
import akka.util.Timeout
import akka.pattern.{ask, pipe}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import de.thm.arsnova.shared.management.RegistryCommands._
import de.thm.arsnova.shared.servicecommands.AuthCommands._
import de.thm.arsnova.shared.entities.Token

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class AuthServiceClientActor extends Actor {
  val serviceType = "auth"

  val manager = context.actorSelection("/user/manager")

  var authRouter: Option[ActorRef] = None

  val tokenList: collection.mutable.HashMap[String, UUID] =
    collection.mutable.HashMap.empty[String, UUID]

  implicit val ec: ExecutionContext = context.dispatcher

  implicit val timeout: Timeout = 5.seconds

  val receive: Receive = start

  def loginUser(m: LoginUser, ret: ActorRef): Unit = {
    (authRouter.get ? m).mapTo[Try[UUID]] pipeTo ret
  }

  def authenticateUser(m: AuthenticateUser, ret: ActorRef): Unit = {
    (authRouter.get ? m).mapTo[Option[Token]] map {
      case Some(token) => {
        tokenList += token.token -> token.userId
        ret ! token.userId
      }
    }
  }

  def start: Receive = {
    case m @ LoginUser => ((ret: ActorRef) => {
      (manager ? GetActorRefForService(serviceType)).mapTo[ActorRef].map { ref =>
        authRouter = Some(ref)
        loginUser(m, ret)
        context.become(gotRef)
      }
    }) (sender)
    case m @ AuthenticateUser(token) => ((ret: ActorRef) => {
      (manager ? GetActorRefForService(serviceType)).mapTo[ActorRef].map { ref =>
        authRouter = Some(ref)
        authenticateUser(m, ret)
        context.become(gotRef)
      }
    }) (sender)
  }

  def gotRef: Receive = {
    case m @ LoginUser => ((ret: ActorRef) => {
      loginUser(m, ret)
    }) (sender)
    case m @ AuthenticateUser(token) => ((ret: ActorRef) => {
      tokenList.get(token) match {
        case Some(id) => ret ! Some(id)
        case None => authenticateUser(m, ret)
      }
    }) (sender)
  }
}
