package de.thm.arsnova.eventservice

import akka.actor.ActorRef
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.events.RoomEvents.{RoomCreated, RoomDeleted}

object BasicEventRouting {
  import ShardRegions._

  def broadcast(sep: RoomEventPackage) = {
    sep.event match {
      case RoomCreated(room) => {
        // usershard is based on userId
        userRegion ! RoomEventPackage(room.userId.get, sep.event)
        commentRegion ! sep
      }
      case RoomDeleted(room) => {
        // usershard is based on userId
        userRegion ! RoomEventPackage(room.userId.get, sep.event)
        commentRegion ! sep
      }

      case ContentCreated(content) => {
        roomRegion ! RoomEventPackage(content.roomId, sep.event)
        answerListRegion ! RoomEventPackage(content.id.get, sep.event)
      }
      case ContentDeleted(content) => {
        roomRegion ! RoomEventPackage(content.roomId, sep.event)
        answerListRegion ! RoomEventPackage(content.id.get, sep.event)
      }
      case NewRound(contentId, round) => {
        answerListRegion ! RoomEventPackage(contentId, sep.event)
      }
    }
  }
}
