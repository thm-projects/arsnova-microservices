package de.thm.arsnova.shared.shards

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands.FreetextAnswerCommand
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands.ChoiceAnswerCommand

object AnswerListShard {
  val shardName = "AnswerList"

  val serviceRole = Some("session")

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: FreetextAnswerCommand => (cmd.questionId.toString, cmd)
    case cmd: ChoiceAnswerCommand => (cmd.questionId.toString, cmd)
    case event: SessionEventPackage => (event.id.toString, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: FreetextAnswerCommand => math.abs(cmd.questionId.hashCode() % 100).toString
    case cmd: ChoiceAnswerCommand => math.abs(cmd.questionId.hashCode() % 100).toString
    case event: SessionEventPackage => math.abs(event.id.hashCode() % 100).toString
  }
}