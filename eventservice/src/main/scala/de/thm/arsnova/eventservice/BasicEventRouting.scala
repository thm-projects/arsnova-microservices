package de.thm.arsnova.eventservice

import akka.actor.ActorRef
import de.thm.arsnova.shared.events.ContentEvents.{ContentCreated, ContentDeleted}
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.events.RoomEvents.{RoomCreated, RoomDeleted}

object BasicEventRouting {
  import ShardRegions._

  def broadcast(sep: RoomEventPackage) = {
    sep.event match {
      case RoomCreated(room) => {
        // usershard is based on userId
        userRegion ! RoomEventPackage(room.userId, sep.event)
        contentListRegion ! sep
        commentRegion ! sep
      }
      case RoomDeleted(room) => {
        // usershard is based on userId
        userRegion ! RoomEventPackage(room.userId, sep.event)
        contentListRegion ! sep
        commentRegion ! sep
      }

      case ContentCreated(content) => {
        answerListRegion ! RoomEventPackage(content.id.get, sep.event)
      }
      case ContentDeleted(content) => {
        answerListRegion ! RoomEventPackage(content.id.get, sep.event)
      }
    }
  }
}
