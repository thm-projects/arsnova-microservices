package de.thm.arsnova.gateway.sharding

import akka.cluster.sharding.ClusterSharding
import akka.actor.ActorRef
import de.thm.arsnova.contentservice.ContentListActor

object ContentShard {
  import de.thm.arsnova.gateway.Context.system

  var proxy: Option[ActorRef] = None

  def startProxy: ActorRef = ClusterSharding(system).startProxy(
    typeName = ContentListActor.shardName,
    role = Some("content"),
    extractEntityId = ContentListActor.idExtractor,
    extractShardId = ContentListActor.shardResolver)

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
