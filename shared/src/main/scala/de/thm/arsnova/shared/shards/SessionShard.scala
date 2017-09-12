package de.thm.arsnova.shared.shards

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.servicecommands.SessionCommands.SessionCommand

object SessionShard {
  val shardName = "Session"

  val serviceRole = Some("session")

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: SessionCommand => (cmd.id.toString, cmd)
    case event: SessionEventPackage => (event.id.toString, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: SessionCommand => math.abs(cmd.id.hashCode() % 100).toString
    case event: SessionEventPackage => math.abs(event.id.hashCode() % 100).toString
  }
}
