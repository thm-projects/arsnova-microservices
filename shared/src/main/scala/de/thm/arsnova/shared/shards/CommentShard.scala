package de.thm.arsnova.shared.shards
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.servicecommands.CommentCommands.CommentCommand

object CommentShard {
  val shardName = "Comment"

  val serviceRole = Some("session")

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: CommentCommand => (cmd.sessionId.toString, cmd)
    case event: SessionEventPackage => (event.id.toString, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: CommentCommand => math.abs(cmd.sessionId.hashCode() % 100).toString
    case event: SessionEventPackage => math.abs(event.id.hashCode() % 100).toString
  }
}
