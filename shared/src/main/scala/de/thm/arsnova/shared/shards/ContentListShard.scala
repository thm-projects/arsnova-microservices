package de.thm.arsnova.shared.shards

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.servicecommands.ContentCommands.ContentCommand

object ContentListShard {
  val shardName = "Question"

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: ContentCommand => (cmd.sessionid.toString, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: ContentCommand => math.abs(cmd.sessionid.hashCode() % 100).toString
  }
}