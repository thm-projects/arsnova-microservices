package de.thm.arsnova.gateway.sharding

import akka.cluster.sharding.ClusterSharding
import akka.actor.ActorRef
import de.thm.arsnova.shared.shards

object UserShard {
  import de.thm.arsnova.gateway.Context.system

  var proxy: Option[ActorRef] = None

  def startProxy: ActorRef = ClusterSharding(system).startProxy(
      typeName = shards.UserShard.shardName,
      role = Some("auth"),
      extractEntityId = shards.UserShard.idExtractor,
      extractShardId = shards.UserShard.shardResolver)

  def getProxy: ActorRef = {
    proxy match {
      case Some(ref) => ref
      case None => {
        proxy = Some(startProxy)
        proxy.get
      }
    }
  }
}
