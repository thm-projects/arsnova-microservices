package de.thm.arsnova.sessionservice

import java.util.UUID

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.routing.RandomPool

import de.thm.arsnova.shared.servicecommands.SessionCommands._
import de.thm.arsnova.shared.servicecommands.AuthCommands._
import de.thm.arsnova.shared.servicecommands.CommandWithToken
import de.thm.arsnova.shared.management.CommandPackage
import de.thm.arsnova.shared.entities.{Session, User}


class SessionServiceActor extends Actor {
  implicit val ex: ExecutionContext = context.system.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val authRouter = context.actorSelection("/user/AuthRouter")

  def tokenToUser(tokenstring: Option[String]): Future[Option[User]] = tokenstring match {
    case Some(t) => (authRouter ? GetUserFromTokenString(t)).mapTo[Option[User]]
    case None => Future { None }
  }

  def receive = {
    case CommandWithToken(command, token) => ((ret: ActorRef) => {
      command match {
        case GetSession(id) => {
          println("yo")
          SessionRepository.findById(id) pipeTo ret
        }
        case GetSessionByKeyword(keyword) => {
          SessionRepository.findByKeyword(keyword) pipeTo ret
        }
        case CreateSession(session) => {
          tokenToUser(token) map { user =>
            SessionRepository.create(session, user) pipeTo ret
          }
        }
      }
    }) (sender)

    case CommandPackage(command, user, returnRef) => {
      command match {
        case GetSession(id) => {
          SessionRepository.findById(id) pipeTo returnRef
        }
        case GetSessionByKeyword(keyword) => {
          SessionRepository.findByKeyword(keyword) pipeTo returnRef
        }
        case CreateSession(session) => {
          SessionRepository.create(session, user) pipeTo returnRef
        }
      }
    }
  }
}
