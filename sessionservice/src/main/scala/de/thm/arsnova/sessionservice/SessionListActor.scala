package de.thm.arsnova.sessionservice

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import de.thm.arsnova.shared.servicecommands.SessionCommands._

import scala.concurrent.ExecutionContext
import scala.util.Random

class SessionListActor extends Actor with ActorLogging {
  import DistributedPubSubMediator.{ Subscribe, SubscribeAck, Publish }
  val KEYLENGTH = 8

  val pubsubChannel = "sessionlist"

  implicit val ec: ExecutionContext = context.dispatcher

  val keys: collection.mutable.HashMap[String, UUID] = collection.mutable.HashMap.empty[String, UUID]

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(pubsubChannel, self)

  def putKVToMap(keyword: String, id: UUID) = {
    keys.put(keyword, id) match {
      case Some(oldid) =>
        if (id != oldid) {
          log.warning(s"id for keyword $keyword got overridden")
        }
    }
  }

  override def preStart(): Unit = {
    SessionRepository.getKeywordList().map { tuples: Seq[(String, UUID)] =>
      tuples.foreach {t =>
        putKVToMap(t._1, t._2)
      }
    }
  }

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
    // pub sub messages
    case SubscribeAck(Subscribe("sessionlist", None, `self`)) =>
      log.info("subscribed to sessionlist")
    case SessionListEntry(id, keyword) =>
      putKVToMap(keyword, id)
    case GetSessionList(ref) =>
      val list = keys.toSeq map {
        case (k, v) => SessionListEntry(v, k)
      }
      ref ! SessionList(list)

    // business logic messages
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
          mediator ! Publish(pubsubChannel, SessionListEntry(id, keyword))
        }
      }
    }) (sender)
  }
}
