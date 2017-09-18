package de.thm.arsnova.shared.shards

import akka.actor.Props
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.servicecommands.UserCommands.UserCommand

object UserShard {
  val shardName = "User"

  val serviceRole = Some("room")

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: UserCommand => (cmd.userId.toString, cmd)
    case event: RoomEventPackage => (event.id.toString, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: UserCommand => math.abs(cmd.userId.hashCode() % 100).toString
    case event: RoomEventPackage => math.abs(event.id.hashCode() % 100).toString
  }
}