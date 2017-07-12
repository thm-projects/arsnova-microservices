package de.thm.arsnova.sessionservice

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorLogging}
import de.thm.arsnova.shared.servicecommands.SessionCommands._

import scala.util.Random

class SessionListActor extends Actor with ActorLogging {
  val KEYLENGTH = 8

  val keys: collection.mutable.HashMap[String, UUID] = collection.mutable.HashMap.empty[String, UUID]

  def generateUniqueKeyword(tries: Int): Option[String] = {
    if (tries < 10) {
      val intList = for (i <- 1 to KEYLENGTH) yield Random.nextInt(10)
      val keyword = intList.map(_.toString).mkString("")
      keys.get(keyword) match {
        case Some(k) => generateUniqueKeyword(tries + 1)
        case None => Some(keyword)
      }
    } else {
      None
    }
  }

  def receive = {
    case LookupSession(keyword) => ((ret: ActorRef) => {
      keys.get(keyword) match {
        case Some(id) => ret ! SessionIdFromKeyword(Some(id))
        case None => ret ! SessionIdFromKeyword(None)
      }
    }) (sender)
    case GenerateKeyword(id) => ((ret: ActorRef) => {
      generateUniqueKeyword(0) match {
        case Some(keyword) => {
          keys += keyword -> id
          ret ! NewKeyword(keyword)
        }
      }
    }) (sender)
  }
}
