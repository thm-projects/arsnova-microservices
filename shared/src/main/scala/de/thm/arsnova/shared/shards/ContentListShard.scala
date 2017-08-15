package de.thm.arsnova.shared.shards

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.servicecommands.ContentCommands.ContentCommand

object ContentListShard {
  val shardName = "Question"

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: ContentCommand => (cmd.sessionId.toString, cmd)
    case event: SessionEventPackage => (event.id.toString, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: ContentCommand => math.abs(cmd.sessionId.hashCode() % 100).toString
    case event: SessionEventPackage => math.abs(event.id.hashCode() % 100).toString
  }
}