package de.thm.arsnova.eventservice

import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import de.thm.arsnova.shared.shards._

object ShardRegions {
  import Context._

  ClusterSharding(system).startProxy(
    typeName = UserShard.shardName,
    role = UserShard.serviceRole,
    extractEntityId = UserShard.idExtractor,
    extractShardId = UserShard.shardResolver
  )

  val userRegion = ClusterSharding(system).shardRegion(UserShard.shardName)

  ClusterSharding(system).startProxy(
    typeName = ContentShard.shardName,
    role = ContentShard.serviceRole,
    extractEntityId = ContentShard.idExtractor,
    extractShardId = ContentShard.shardResolver
  )

  val contentRegion = ClusterSharding(system).shardRegion(ContentShard.shardName)

  ClusterSharding(system).startProxy(
    typeName = RoomShard.shardName,
    role = RoomShard.serviceRole,
    extractEntityId = RoomShard.idExtractor,
    extractShardId = RoomShard.shardResolver
  )

  val roomRegion = ClusterSharding(system).shardRegion(RoomShard.shardName)

  ClusterSharding(system).startProxy(
    typeName = CommentShard.shardName,
    role = CommentShard.serviceRole,
    extractEntityId = CommentShard.idExtractor,
    extractShardId = CommentShard.shardResolver
  )

  val commentRegion = ClusterSharding(system).shardRegion(CommentShard.shardName)

  ClusterSharding(system).startProxy(
    typeName = AnswerListShard.shardName,
    role = AnswerListShard.serviceRole,
    extractEntityId = AnswerListShard.idExtractor,
    extractShardId = AnswerListShard.shardResolver
  )

  val answerListRegion = ClusterSharding(system).shardRegion(AnswerListShard.shardName)
}
