package de.thm.arsnova.keywordservice

import akka.actor.Props
import akka.routing.RoundRobinPool
import kamon.Kamon
import de.thm.arsnova.shared.actors.ServiceManagementActor

object KeywordService extends App with MigrationConfig {
  import Context._

  if (args.contains("kamon")) {
    Kamon.start()
  }

  if (args.contains("migrate")) {
    migrate()
  }
  if (args.contains("cleanDB")) {
    reloadSchema()
  }

  val router = system.actorOf(RoundRobinPool(10).props(Props[RoomListActor]), "router")
  val manager = system.actorOf(ServiceManagementActor.props(Seq(("roomlist", router))), "manager")
}
