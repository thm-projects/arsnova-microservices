package de.thm.arsnova.contentservice

import akka.actor.{ActorPath, Props}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.routing.RandomPool
import de.thm.arsnova.authservice.{AuthServiceActor, UserActor}
import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.shards.{ContentListShard, UserShard}

import scala.concurrent.Await
import scala.concurrent.duration._

object ContentService extends App {
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

  val storeRef = Await.result(system.actorSelection(ActorPath.fromString("akka://ARSnovaService@127.0.0.1:8870/user/store")).resolveOne, 5.seconds)
  SharedLeveldbJournal.setStore(storeRef, system)

  ClusterSharding(system).start(
    typeName = ContentListShard.shardName,
    entityProps = ContentListActor.props(authRouter, userRegion),
    settings = ClusterShardingSettings(system),
    extractEntityId = ContentListShard.idExtractor,
    extractShardId = ContentListShard.shardResolver)

  val manager = system.actorOf(ServiceManagementActor.props(Nil), "manager")
}