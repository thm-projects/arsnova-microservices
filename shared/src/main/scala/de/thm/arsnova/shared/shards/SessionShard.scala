package de.thm.arsnova.shared.shards

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.servicecommands.SessionCommands.SessionCommand

object SessionShard {
  val shardName = "Session"

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: SessionCommand => (cmd.id.toString, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: SessionCommand => math.abs(cmd.id.hashCode() % 100).toString
  }
}
