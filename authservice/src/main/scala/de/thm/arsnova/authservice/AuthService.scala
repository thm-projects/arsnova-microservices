package de.thm.arsnova.authservice

import akka.actor.{ActorPath, Props, ActorRef}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.routing.RandomPool
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.shards.{SessionShard, UserShard}

import scala.concurrent.Await
import scala.concurrent.duration._

object AuthService extends App with MigrationConfig {
  import Context._

  val storeRef = Await.result(system.actorSelection(ActorPath.fromString("akka://ARSnovaService@127.0.0.1:8870/user/store")).resolveOne, 5.seconds)
  SharedLeveldbJournal.setStore(storeRef, system)

  ClusterSharding(system).startProxy(
    typeName = SessionShard.shardName,
    role = SessionShard.serviceRole,
    extractEntityId = SessionShard.idExtractor,
    extractShardId = SessionShard.shardResolver)

  val sessionRegion = ClusterSharding(system).shardRegion(SessionShard.shardName)

  val authRouter: ActorRef = system.actorOf(RandomPool(100).props(Props[AuthServiceActor]), "authRouter")

  val manager = system.actorOf(ServiceManagementActor.props(Seq(("auth", authRouter))), "manager")

  if (args.contains("migrate")) {
    migrate()
  }
  if (args.contains("cleanDB")) {
    reloadSchema()
  }
}
