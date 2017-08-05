package de.thm.arsnova.eventservice

import scala.concurrent.duration._
import akka.actor.ActorPath
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import de.thm.arsnova.shared.shards.EventShard

import scala.concurrent.Await

object EventService extends App {
  import Context._

  val storeRef = Await.result(system.actorSelection(ActorPath.fromString("akka://ARSnovaService@127.0.0.1:8870/user/store")).resolveOne, 5.seconds)
  SharedLeveldbJournal.setStore(storeRef, system)

  ClusterSharding(system).start(
    typeName = EventShard.shardName,
    entityProps = SessionEventActor.props(),
    settings = ClusterShardingSettings(system),
    extractEntityId = EventShard.idExtractor,
    extractShardId = EventShard.shardResolver
  )
}

