package de.thm.arsnova.sessionservice

import akka.actor.{ActorIdentity, ActorPath, ActorSystem, Identify, Props}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.routing.{ConsistentHashingPool, RandomPool}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration._
import kamon.Kamon
import de.thm.arsnova.authservice.{AuthServiceActor, UserActor}
import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.shards.{EventShard, SessionShard, UserShard}

object SessionService extends App {
  import Context._

  val authRouter = system.actorOf(
    ClusterRouterPool(new RandomPool(10), ClusterRouterPoolSettings(
      totalInstances = 10,
      maxInstancesPerNode = 5,
      allowLocalRoutees = false,
      useRole = Some("auth")
    )).props(Props[AuthServiceActor]), "AuthRouter"
  )

  ClusterSharding(system).startProxy(
    typeName = UserShard.shardName,
    role = Some("auth"),
    extractEntityId = UserShard.idExtractor,
    extractShardId = UserShard.shardResolver
  )

  val userRegion = ClusterSharding(system).shardRegion(UserShard.shardName)

  ClusterSharding(system).startProxy(
    typeName = EventShard.shardName,
    role = Some("event"),
    extractEntityId = EventShard.idExtractor,
    extractShardId = EventShard.shardResolver
  )

  val eventRegion = ClusterSharding(system).shardRegion(EventShard.shardName)

  val storeRef = Await.result(system.actorSelection(ActorPath.fromString("akka://ARSnovaService@127.0.0.1:8870/user/store")).resolveOne, 5.seconds)
  SharedLeveldbJournal.setStore(storeRef, system)

  ClusterSharding(system).start(
    typeName = SessionShard.shardName,
    entityProps = SessionActor.props(eventRegion,authRouter, userRegion),
    settings = ClusterShardingSettings(system),
    extractEntityId = SessionShard.idExtractor,
    extractShardId = SessionShard.shardResolver
  )
}