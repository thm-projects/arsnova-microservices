package de.thm.arsnova.eventservice

import akka.actor.ActorRef
import de.thm.arsnova.shared.events.SessionEventPackage
import de.thm.arsnova.shared.events.SessionEvents.SessionCreated

object BasicEventRouting {
  import ShardRegions._
  val routing: collection.mutable.HashMap[String, Seq[ActorRef]] =
    collection.mutable.HashMap(
      "SessionCreated" -> Seq(userRegion)
    )

  def broadcast(sep: SessionEventPackage) = {
    sep.event match {
      case SessionCreated(session) => {
        userRegion ! SessionEventPackage(session.userId, sep.event)
      }
    }
  }
}
