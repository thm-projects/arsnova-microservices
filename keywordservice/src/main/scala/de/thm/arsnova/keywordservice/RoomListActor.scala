package de.thm.arsnova.keywordservice

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import de.thm.arsnova.shared.servicecommands.KeywordCommands._

import scala.concurrent.ExecutionContext

class RoomListActor extends Actor with ActorLogging {
  implicit val ex: ExecutionContext = context.system.dispatcher

  def receive = {
    case LookupRoom(keyword) =>
      RoomListEntryRepository.getEntryFromId(keyword) pipeTo sender()
    case GenerateEntry => ((ret: ActorRef) => {
      RoomListEntryRepository.create() match {
        case Some(roomEntry) => ret ! roomEntry
        case None => println("room entry couldn't get generated")
      }
    }) (sender)
  }
}
