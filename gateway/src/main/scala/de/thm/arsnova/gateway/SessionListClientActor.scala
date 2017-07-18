package de.thm.arsnova.gateway

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey, Replicator}
import akka.cluster.ddata.Replicator._
import akka.util.Timeout
import de.thm.arsnova.shared.servicecommands.SessionCommands._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

class SessionListClientActor extends Actor with ActorLogging {
  val dChannel = "sessionlist"

  val sessionList: collection.mutable.HashMap[String, UUID] =
    collection.mutable.HashMap.empty[String, UUID]

  implicit val ec: ExecutionContext = context.dispatcher

  implicit val timeout: Timeout = 5.seconds

  implicit val cluster = Cluster(context.system)

  val replicator = DistributedData(context.system).replicator

  val dataKey = LWWMapKey[String, UUID](dChannel)

  replicator ! Subscribe(dataKey, self)

  def receive = {
    // ddata messages
    case g @ GetSuccess(LWWMapKey(_), Some(GetSessionEntry(keyword, ref))) =>
      println(g.dataValue)
    case NotFound(_, Some(GetSessionEntry(keyword, ref))) =>
      ref ! SessionIdFromKeyword(None)
    case c @ Changed(dataKey) =>
      println("changed dataKey")

    // business logic messages
    case LookupSession(keyword) => {
      replicator ! Get(dataKey, ReadLocal, Some(GetSessionEntry(keyword, sender())))
    }
  }
}
