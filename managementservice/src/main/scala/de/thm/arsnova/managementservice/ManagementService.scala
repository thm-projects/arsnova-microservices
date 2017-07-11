package de.thm.arsnova.managementservice

import akka.actor.Props
import akka.persistence.journal.leveldb.SharedLeveldbStore

object ManagementService extends App {
  import Context._

  val store = system.actorOf(Props[SharedLeveldbStore], "store")

  val registry = system.actorOf(Props[ServiceRegistryActor], name = "registry")
}