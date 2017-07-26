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

object SessionService extends App with MigrationConfig {
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
    typeName = UserActor.shardName,
    role = Some("auth"),
    extractEntityId = UserActor.idExtractor,
    extractShardId = UserActor.shardResolver
  )

  val userRegion = ClusterSharding(system).shardRegion(UserActor.shardName)

  val storeRef = Await.result(system.actorSelection(ActorPath.fromString("akka://ARSnovaService@127.0.0.1:8870/user/store")).resolveOne, 5.seconds)
  SharedLeveldbJournal.setStore(storeRef, system)

  ClusterSharding(system).start(
    typeName = SessionActor.shardName,
    entityProps = SessionActor.props(authRouter, userRegion),
    settings = ClusterShardingSettings(system),
    extractEntityId = SessionActor.idExtractor,
    extractShardId = SessionActor.shardResolver
  )

  if (args.contains("kamon")) {
    Kamon.start()
  }

  if (args.contains("migrate")) {
    migrate()
  }
  if (args.contains("cleanDB")) {
    reloadSchema()
  }

  // val manager = system.actorOf(ServiceManagementActor.props("session", dispatcher), "manager")
}