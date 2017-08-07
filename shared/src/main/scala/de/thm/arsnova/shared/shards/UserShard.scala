package de.thm.arsnova.shared.shards

import akka.actor.Props
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.servicecommands.UserCommands.UserCommand

object UserShard {
  val shardName = "User"

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: UserCommand => (cmd.userId.toString, cmd)
    case event: SessionEventPackage => (event.id.toString, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: UserCommand => math.abs(cmd.userId.hashCode() % 100).toString
    case event: SessionEventPackage => math.abs(event.id.hashCode() % 100).toString
  }
}