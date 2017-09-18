package de.thm.arsnova.shared.shards

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.servicecommands.RoomCommands.RoomCommand

object RoomShard {
  val shardName = "Room"

  val serviceRole = Some("room")

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: RoomCommand => (cmd.id.toString, cmd)
    case event: RoomEventPackage => (event.id.toString, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: RoomCommand => math.abs(cmd.id.hashCode() % 100).toString
    case event: RoomEventPackage => math.abs(event.id.hashCode() % 100).toString
  }
}
