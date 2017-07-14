package de.thm.arsnova.gateway

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorLogging}
import akka.cluster.pubsub.{DistributedPubSubMediator, DistributedPubSub}
import de.thm.arsnova.shared.servicecommands.SessionCommands._

import scala.util.Random

class SessionListClientActor extends Actor with ActorLogging {
  import DistributedPubSubMediator.{ Subscribe, SubscribeAck, Publish }

  val pubsubChannel = "sessionlist"

  val keys: collection.mutable.HashMap[String, UUID] = collection.mutable.HashMap.empty[String, UUID]

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(pubsubChannel, self)

  def receive = {
    // pub sub messages
    case SubscribeAck(Subscribe("sessionlist", None, `self`)) =>
      log.info("subscribed to sessionlist")
    case SessionListEntry(id, keyword) =>
      keys.put(keyword, id) match {
        case Some(oldid) =>
          if (id != oldid) {
            log.warning(s"id for keyword $keyword got overridden")
          }
      }

    // business logic messages
    case LookupSession(keyword) => ((ret: ActorRef) => {
      keys.get(keyword) match {
        case Some(id) => ret ! SessionIdFromKeyword(Some(id))
        case None => ret ! SessionIdFromKeyword(None)
      }
    }) (sender)
  }
}
