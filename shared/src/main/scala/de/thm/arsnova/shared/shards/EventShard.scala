package de.thm.arsnova.shared.shards

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.servicecommands.EventCommands.EventCommand

object EventShard {
  val shardName = "Event"

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: EventCommand => (cmd.id.toString, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: EventCommand => math.abs(cmd.id.hashCode() % 100).toString
  }
}
