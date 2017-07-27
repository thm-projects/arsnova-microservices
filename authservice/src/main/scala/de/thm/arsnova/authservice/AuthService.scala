package de.thm.arsnova.authservice

import akka.actor.{ActorPath, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import de.thm.arsnova.shared.actors.ServiceManagementActor
import de.thm.arsnova.shared.shards.UserShard

import scala.concurrent.Await
import scala.concurrent.duration._

object AuthService extends App with MigrationConfig {
  import Context._

  val storeRef = Await.result(system.actorSelection(ActorPath.fromString("akka://ARSnovaService@127.0.0.1:8870/user/store")).resolveOne, 5.seconds)
  SharedLeveldbJournal.setStore(storeRef, system)

  ClusterSharding(system).start(
    typeName = UserShard.shardName,
    entityProps = UserActor.props(),
    settings = ClusterShardingSettings(system),
    extractEntityId = UserShard.idExtractor,
    extractShardId = UserShard.shardResolver)

  val auth = system.actorOf(Props[AuthServiceActor], name = "auth")
  val manager = system.actorOf(ServiceManagementActor.props(Seq(("auth", auth))), "manager")

  if (args.contains("migrate")) {
    migrate()
  }
  if (args.contains("cleanDB")) {
    reloadSchema()
  }
}
