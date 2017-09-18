package de.thm.arsnova.gateway

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.Cluster
import akka.util.Timeout
import akka.pattern.ask
import de.thm.arsnova.shared.Exceptions.NoSuchRoom
import de.thm.arsnova.shared.servicecommands.KeywordCommands._
import de.thm.arsnova.shared.management.RegistryCommands._
import de.thm.arsnova.shared.entities.RoomListEntry

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

class RoomListClientActor extends Actor with ActorLogging {
  val serviceType = "roomlist"

  val manager = context.actorSelection("/user/manager")

  var roomLister: Option[ActorRef] = None

  val roomList: collection.mutable.HashMap[String, UUID] =
    collection.mutable.HashMap.empty[String, UUID]

  implicit val ec: ExecutionContext = context.dispatcher

  implicit val timeout: Timeout = 5.seconds

  implicit val cluster = Cluster(context.system)

  def receive: Receive = start

  def lookupRoom(m: LookupRoom, ret: ActorRef): Unit = {
    (roomLister.get ? m).mapTo[Option[RoomListEntry]].map {
      case Some(entry) => {
        // store in cache
        roomList += (entry.keyword -> entry.id)
        ret ! Some(entry.id)
      }
      case None => ret ! None
    }
  }

  def generateEntry(ret: ActorRef): Unit = {
    (roomLister.get ? GenerateEntry).mapTo[RoomListEntry].map { s =>
      roomList += (s.keyword -> s.id)
      ret ! s
    }
  }

  def start: Receive = {
    case m @ LookupRoom(keyword) => ((ret: ActorRef) => {
      // check cache
      (manager ? GetActorRefForService(serviceType)).mapTo[ActorRef].map { ref =>
        roomLister = Some(ref)
        lookupRoom(m, ret)
        context.become(gotRoomListActor)
      }
    }) (sender)
    case m @ GenerateEntry => ((ret: ActorRef) => {
      (manager ? GetActorRefForService(serviceType)).mapTo[ActorRef].map { ref =>
        roomLister = Some(ref)
        generateEntry(ret)
        context.become(gotRoomListActor)
      }
    }) (sender)
  }

  def gotRoomListActor: Receive = {
    // business logic messages
    case m @ LookupRoom(keyword) => ((ret: ActorRef) => {
      // check cache
      roomList.get(keyword) match {
        case Some(id) => ret ! Some(id)
        // ask keyword service about keyword
        case None => lookupRoom(m, ret)
      }
    }) (sender)
    case m @ GenerateEntry => ((ret: ActorRef) => {
      generateEntry(ret)
    }) (sender)
  }
}
