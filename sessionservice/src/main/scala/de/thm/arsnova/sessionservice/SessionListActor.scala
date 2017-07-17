package de.thm.arsnova.sessionservice

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

class SessionListActor extends Actor with ActorLogging {
  val KEYLENGTH = 8

  val dChannel = "sessionlist"

  implicit val ec: ExecutionContext = context.dispatcher

  implicit val timeout: Timeout = 5.seconds

  implicit val cluster = Cluster(context.system)

  val keys: collection.mutable.HashMap[String, UUID] =
    collection.mutable.HashMap.empty[String, UUID]

  val replicator = DistributedData(context.system).replicator

  val dataKey = LWWMapKey[String, UUID](dChannel)

  replicator ! Subscribe(dataKey, self)

  override def preStart(): Unit = {
    SessionRepository.getKeywordList().map { tuples: Seq[(String, UUID)] =>
      tuples.foreach {t =>

      }
    }
  }

  def generateUniqueKeyword(tries: Int): Option[String] = {
    if (tries < 10) {
      val intList = for (i <- 1 to KEYLENGTH) yield Random.nextInt(10)
      val keyword = intList.map(_.toString).mkString("")
      /*keys.get(keyword) match {
        case Some(k) => generateUniqueKeyword(tries + 1)
        case None => Some(keyword)
      }*/
      // TODO: Check whether key already exists!
      Some(keyword)
    } else {
      None
    }
  }

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
    case GenerateKeyword(id) => ((ret: ActorRef) => {
      generateUniqueKeyword(0) match {
        case Some(keyword) => {
          replicator ! Update(dataKey, LWWMap(), WriteAll(timeout = 5.seconds))(_ + (keyword -> id))
          ret ! NewKeyword(keyword)
        }
      }
    }) (sender)
  }
}
