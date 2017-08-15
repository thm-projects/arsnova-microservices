package de.thm.arsnova.eventservice

import akka.actor.ActorRef
import de.thm.arsnova.shared.events.ContentEvents.{ContentCreated, ContentDeleted}
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.events.SessionEvents.{SessionCreated, SessionDeleted}

object BasicEventRouting {
  import ShardRegions._

  def broadcast(sep: SessionEventPackage) = {
    sep.event match {
      case SessionCreated(session) => {
        // usershard is based on userId
        userRegion ! SessionEventPackage(session.userId, sep.event)
        contentListRegion ! sep
        commentRegion ! sep
      }
      case SessionDeleted(session) => {
        // usershard is based on userId
        userRegion ! SessionEventPackage(session.userId, sep.event)
        contentListRegion ! sep
        commentRegion ! sep
      }

      case ContentCreated(content) => {
        answerListRegion ! SessionEventPackage(content.id.get, sep.event)
      }
      case ContentDeleted(content) => {
        answerListRegion ! SessionEventPackage(content.id.get, sep.event)
      }
    }
  }
}
