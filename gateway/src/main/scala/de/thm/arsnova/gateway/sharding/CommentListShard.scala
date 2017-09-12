package de.thm.arsnova.gateway.sharding

import akka.cluster.sharding.ClusterSharding
import akka.actor.ActorRef
import de.thm.arsnova.shared.shards.CommentShard

object CommentListShard {
  import de.thm.arsnova.gateway.Context.system

  var proxy: Option[ActorRef] = None

  def startProxy: ActorRef = ClusterSharding(system).startProxy(
    typeName = CommentShard.shardName,
    role = Some("session"),
    extractEntityId = CommentShard.idExtractor,
    extractShardId = CommentShard.shardResolver)

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
