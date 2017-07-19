package de.thm.arsnova.gateway

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.Cluster
import akka.util.Timeout
import akka.pattern.ask
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

  override def preStart(): Unit = {
    (manager ? GetActorRefForService(serviceType)).mapTo[ActorRef].map { ref =>
      sessionLister = Some(ref)
    }
  }

  def receive = {
    // business logic messages
    case m @ LookupSession(keyword) => ((ret: ActorRef) => {
      // check cache
      sessionList.get(keyword) match {
        case Some(id) => ret ! id
        // ask keyword service about keyword
        case None => (sessionLister.get ? m).mapTo[UUID].map { id =>
          // store in cache
          sessionList += (keyword -> id)
          ret ! id
        }
      }
    }) (sender)
    case m @ GenerateEntry => ((ret: ActorRef) => {
      (sessionLister.get ? m).mapTo[SessionListEntry].map { s =>
        sessionList += (s.keyword -> s.id)
        ret ! s
      }
    }) (sender)
  }
}
