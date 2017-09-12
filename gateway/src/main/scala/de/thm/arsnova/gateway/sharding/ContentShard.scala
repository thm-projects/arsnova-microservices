package de.thm.arsnova.gateway.sharding

import akka.cluster.sharding.ClusterSharding
import akka.actor.ActorRef
import de.thm.arsnova.shared.shards.ContentListShard

object ContentShard {
  import de.thm.arsnova.gateway.Context.system

  var proxy: Option[ActorRef] = None

  def startProxy: ActorRef = ClusterSharding(system).startProxy(
    typeName = ContentListShard.shardName,
    role = Some("session"),
    extractEntityId = ContentListShard.idExtractor,
    extractShardId = ContentListShard.shardResolver)

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
