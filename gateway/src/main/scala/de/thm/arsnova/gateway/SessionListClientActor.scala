package de.thm.arsnova.gateway

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.Cluster
import akka.util.Timeout
import akka.pattern.ask
import de.thm.arsnova.shared.Exceptions.NoSuchSession
import de.thm.arsnova.shared.servicecommands.KeywordCommands._
import de.thm.arsnova.shared.management.RegistryCommands._
import de.thm.arsnova.shared.entities.SessionListEntry

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

class SessionListClientActor extends Actor with ActorLogging {
  val serviceType = "sessionlist"

  val manager = context.actorSelection("/user/manager")

  var sessionLister: Option[ActorRef] = None

  val sessionList: collection.mutable.HashMap[String, UUID] =
    collection.mutable.HashMap.empty[String, UUID]

  implicit val ec: ExecutionContext = context.dispatcher

  implicit val timeout: Timeout = 5.seconds

  implicit val cluster = Cluster(context.system)

  def receive: Receive = start

  def lookupSession(m: LookupSession, ret: ActorRef): Unit = {
    (sessionLister.get ? m).mapTo[Option[SessionListEntry]].map {
      case Some(entry) => {
        // store in cache
        sessionList += (entry.keyword -> entry.id)
        ret ! Some(entry.id)
      }
      case None => ret ! None
    }
  }

  def generateEntry(ret: ActorRef): Unit = {
    (sessionLister.get ? GenerateEntry).mapTo[SessionListEntry].map { s =>
      sessionList += (s.keyword -> s.id)
      ret ! s
    }
  }

  def start: Receive = {
    case m @ LookupSession(keyword) => ((ret: ActorRef) => {
      // check cache
      (manager ? GetActorRefForService(serviceType)).mapTo[ActorRef].map { ref =>
        sessionLister = Some(ref)
        lookupSession(m, ret)
        context.become(gotSessionListActor)
      }
    }) (sender)
    case m @ GenerateEntry => ((ret: ActorRef) => {
      (manager ? GetActorRefForService(serviceType)).mapTo[ActorRef].map { ref =>
        sessionLister = Some(ref)
        generateEntry(ret)
        context.become(gotSessionListActor)
      }
    }) (sender)
  }

  def gotSessionListActor: Receive = {
    // business logic messages
    case m @ LookupSession(keyword) => ((ret: ActorRef) => {
      // check cache
      sessionList.get(keyword) match {
        case Some(id) => ret ! id
        // ask keyword service about keyword
        case None => lookupSession(m, ret)
      }
    }) (sender)
    case m @ GenerateEntry => ((ret: ActorRef) => {
      generateEntry(ret)
    }) (sender)
  }
}
