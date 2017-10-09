package de.thm.arsnova.gateway.api

import akka.actor.Props

import scala.concurrent.duration._
import akka.util.Timeout
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.gateway.AuthServiceClientActor
import de.thm.arsnova.shared.shards._

trait BaseApi {
  import de.thm.arsnova.gateway.Context.system

  // TODO: why do i need a new implicit val?
  implicit val executionContext = system.dispatcher

  // timeout for actor calls
  implicit val timeout = Timeout(15.seconds)
  // actor for every command
  val remoteCommander = system.actorSelection("akka://CommandService@127.0.0.1:8880/user/commander")
  val authClient = system.actorOf(Props[AuthServiceClientActor], name = "authClient")
  
  val userRegion = ClusterSharding(system).startProxy(
    typeName = UserShard.shardName,
    role = UserShard.serviceRole,
    extractEntityId = UserShard.idExtractor,
    extractShardId = UserShard.shardResolver
  )

  val roomRegion = ClusterSharding(system).startProxy(
    typeName = RoomShard.shardName,
    role = RoomShard.serviceRole,
    extractEntityId = RoomShard.idExtractor,
    extractShardId = RoomShard.shardResolver
  )

  val contentRegion = ClusterSharding(system).startProxy(
    typeName = ContentListShard.shardName,
    role = ContentListShard.serviceRole,
    extractEntityId = ContentListShard.idExtractor,
    extractShardId = ContentListShard.shardResolver
  )

  val answerListRegion = ClusterSharding(system).startProxy(
    typeName = AnswerListShard.shardName,
    role = AnswerListShard.serviceRole,
    extractEntityId = AnswerListShard.idExtractor,
    extractShardId = AnswerListShard.shardResolver
  )

  val commentRegion = ClusterSharding(system).startProxy(
    typeName = CommentShard.shardName,
    role = CommentShard.serviceRole,
    extractEntityId = CommentShard.idExtractor,
    extractShardId = CommentShard.shardResolver
  )
}
