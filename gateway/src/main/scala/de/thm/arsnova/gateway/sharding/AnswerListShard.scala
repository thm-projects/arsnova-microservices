package de.thm.arsnova.gateway.sharding

import akka.cluster.sharding.ClusterSharding
import akka.actor.ActorRef
import de.thm.arsnova.shared.shards.AnswerListShard

object AnswerListShard {
  import de.thm.arsnova.gateway.Context.system

  var proxy: Option[ActorRef] = None

  def startProxy: ActorRef = ClusterSharding(system).startProxy(
    typeName = de.thm.arsnova.shared.shards.AnswerListShard.shardName,
    role = Some("session"),
    extractEntityId = de.thm.arsnova.shared.shards.AnswerListShard.idExtractor,
    extractShardId = de.thm.arsnova.shared.shards.AnswerListShard.shardResolver)

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
