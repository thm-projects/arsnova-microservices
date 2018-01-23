package de.thm.arsnova.shared.shards

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands.FreetextAnswerCommand
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands.ChoiceAnswerCommand

object AnswerListShard {
  val shardName = "AnswerList"

  val serviceRole = Some("room")

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: FreetextAnswerCommand => (cmd.contentId.toString, cmd)
    case cmd: ChoiceAnswerCommand => (cmd.questionId.toString, cmd)
    case event: RoomEventPackage => (event.id.toString, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: FreetextAnswerCommand => math.abs(cmd.contentId.hashCode() % 100).toString
    case cmd: ChoiceAnswerCommand => math.abs(cmd.questionId.hashCode() % 100).toString
    case event: RoomEventPackage => math.abs(event.id.hashCode() % 100).toString
  }
}