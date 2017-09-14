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
import de.thm.arsnova.authservice.AuthServiceActor
import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.shards._

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
    role = UserShard.serviceRole,
    extractEntityId = UserShard.idExtractor,
    extractShardId = UserShard.shardResolver
  )

  ClusterSharding(system).startProxy(
    typeName = EventShard.shardName,
    role = EventShard.serviceRole,
    extractEntityId = EventShard.idExtractor,
    extractShardId = EventShard.shardResolver
  )

  ClusterSharding(system).startProxy(
    typeName = SessionShard.shardName,
    role = SessionShard.serviceRole,
    extractEntityId = SessionShard.idExtractor,
    extractShardId = SessionShard.shardResolver
  )

  ClusterSharding(system).startProxy(
    typeName = ContentListShard.shardName,
    role = ContentListShard.serviceRole,
    extractEntityId = ContentListShard.idExtractor,
    extractShardId = ContentListShard.shardResolver
  )

  val storeRef = Await.result(system.actorSelection(ActorPath.fromString("akka://ARSnovaService@127.0.0.1:8870/user/store")).resolveOne, 5.seconds)
  SharedLeveldbJournal.setStore(storeRef, system)

  ClusterSharding(system).start(
    typeName = UserShard.shardName,
    entityProps = UserActor.props(),
    settings = ClusterShardingSettings(system),
    extractEntityId = UserShard.idExtractor,
    extractShardId = UserShard.shardResolver
  )

  ClusterSharding(system).start(
    typeName = SessionShard.shardName,
    entityProps = SessionActor.props(authRouter),
    settings = ClusterShardingSettings(system),
    extractEntityId = SessionShard.idExtractor,
    extractShardId = SessionShard.shardResolver
  )

  ClusterSharding(system).start(
    typeName = ContentListShard.shardName,
    entityProps = ContentListActor.props(authRouter),
    settings = ClusterShardingSettings(system),
    extractEntityId = ContentListShard.idExtractor,
    extractShardId = ContentListShard.shardResolver
  )

  ClusterSharding(system).start(
    typeName = CommentShard.shardName,
    entityProps = CommentListActor.props(authRouter),
    settings = ClusterShardingSettings(system),
    extractEntityId = CommentShard.idExtractor,
    extractShardId = CommentShard.shardResolver
  )

  ClusterSharding(system).start(
    typeName = AnswerListShard.shardName,
    entityProps = AnswerListActor.props(authRouter),
    settings = ClusterShardingSettings(system),
    extractEntityId = AnswerListShard.idExtractor,
    extractShardId = AnswerListShard.shardResolver
  )
}