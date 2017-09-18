package de.thm.arsnova.shared.shards

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.servicecommands.EventCommands.EventCommand

object EventShard {
  val shardName = "event"

  val serviceRole = Some("event")

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: EventCommand => (cmd.id.toString, cmd)
    case sep: RoomEventPackage => (sep.id.toString, sep)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: EventCommand => math.abs(cmd.id.hashCode() % 100).toString
    case sep: RoomEventPackage => math.abs(sep.id.hashCode() % 100).toString
  }
}
