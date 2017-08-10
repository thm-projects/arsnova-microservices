package de.thm.arsnova.eventservice

import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import de.thm.arsnova.shared.shards._

object ShardRegions {
  import Context._

  ClusterSharding(system).startProxy(
    typeName = UserShard.shardName,
    role = Some("auth"),
    extractEntityId = UserShard.idExtractor,
    extractShardId = UserShard.shardResolver
  )

  val userRegion = ClusterSharding(system).shardRegion(UserShard.shardName)

  ClusterSharding(system).startProxy(
    typeName = ContentListShard.shardName,
    role = Some("content"),
    extractEntityId = ContentListShard.idExtractor,
    extractShardId = ContentListShard.shardResolver
  )

  val contentListRegion = ClusterSharding(system).shardRegion(ContentListShard.shardName)

  ClusterSharding(system).startProxy(
    typeName = SessionShard.shardName,
    role = Some("session"),
    extractEntityId = SessionShard.idExtractor,
    extractShardId = SessionShard.shardResolver
  )

  val sessionRegion = ClusterSharding(system).shardRegion(SessionShard.shardName)

  ClusterSharding(system).startProxy(
    typeName = CommentShard.shardName,
    role = Some("comment"),
    extractEntityId = CommentShard.idExtractor,
    extractShardId = CommentShard.shardResolver
  )

  val commentRegion = ClusterSharding(system).shardRegion(CommentShard.shardName)
}
