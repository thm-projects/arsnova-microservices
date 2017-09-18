package de.thm.arsnova.shared.shards
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.servicecommands.CommentCommands.CommentCommand

object CommentShard {
  val shardName = "Comment"

  val serviceRole = Some("room")

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: CommentCommand => (cmd.roomId.toString, cmd)
    case event: RoomEventPackage => (event.id.toString, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: CommentCommand => math.abs(cmd.roomId.hashCode() % 100).toString
    case event: RoomEventPackage => math.abs(event.id.hashCode() % 100).toString
  }
}
