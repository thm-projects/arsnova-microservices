package de.thm.arsnova.shared.shards

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.servicecommands.ContentCommands.ContentCommand

object ContentListShard {
  val shardName = "Question"

  val serviceRole = Some("room")

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: ContentCommand => (cmd.roomId.toString, cmd)
    case event: RoomEventPackage => (event.id.toString, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: ContentCommand => math.abs(cmd.roomId.hashCode() % 100).toString
    case event: RoomEventPackage => math.abs(event.id.hashCode() % 100).toString
  }
}