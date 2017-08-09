package de.thm.arsnova.commentservice

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.{ActorPath, Props}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import akka.routing.RandomPool
import de.thm.arsnova.authservice.AuthServiceActor
import de.thm.arsnova.shared.shards.{CommentShard, EventShard, SessionShard, UserShard}

import scala.concurrent.Await

object CommentService extends App {
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

  ClusterSharding(system).startProxy(
    typeName = EventShard.shardName,
    role = Some("event"),
    extractEntityId = EventShard.idExtractor,
    extractShardId = EventShard.shardResolver
  )

  ClusterSharding(system).startProxy(
    typeName = SessionShard.shardName,
    role = Some("session"),
    extractEntityId = SessionShard.idExtractor,
    extractShardId = SessionShard.shardResolver)

  val sessionRegion = ClusterSharding(system).shardRegion(SessionShard.shardName)

  val userRegion = ClusterSharding(system).shardRegion(UserShard.shardName)

  val eventRegion = ClusterSharding(system).shardRegion(EventShard.shardName)

  val storeRef = Await.result(system.actorSelection(ActorPath.fromString("akka://ARSnovaService@127.0.0.1:8870/user/store")).resolveOne, 5.seconds)
  SharedLeveldbJournal.setStore(storeRef, system)

  ClusterSharding(system).start(
    typeName = CommentShard.shardName,
    entityProps = CommentListActor.props(eventRegion, authRouter, sessionRegion),
    settings = ClusterShardingSettings(system),
    extractEntityId = CommentShard.idExtractor,
    extractShardId = CommentShard.shardResolver)
}