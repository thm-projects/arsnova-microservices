package de.thm.arsnova.keywordservice

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import de.thm.arsnova.shared.servicecommands.KeywordCommands._

import scala.concurrent.ExecutionContext

class SessionListActor extends Actor with ActorLogging {
  implicit val ex: ExecutionContext = context.system.dispatcher

  def receive = {
    case LookupSession(keyword) =>
      SessionListEntryRepository.getEntryFromId(keyword) pipeTo sender()
    case GenerateEntry => ((ret: ActorRef) => {
      SessionListEntryRepository.create() match {
        case Some(sessionEntry) => ret ! sessionEntry
        case None => println("session entry couldn't get generated")
      }
    }) (sender)
  }
}
