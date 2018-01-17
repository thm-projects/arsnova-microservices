package de.thm.arsnova.shared.servicecommands

import akka.actor.ActorRef
import de.thm.arsnova.shared.entities.{Content, ContentGroup}

object ContentGroupCommands {
  sealed trait ContentGroupCommand extends ServiceCommand

  case class SetGroups(groups: Map[String, ContentGroup])

  case class AddToGroup(group: String, content: Content) extends ContentGroupCommand

  case class RemoveFromGroup(group: String, content: Content) extends ContentGroupCommand

  case class SendContent(ret: ActorRef, group: Option[String]) extends ContentGroupCommand

  case class GetExportList() extends ContentGroupCommand
}
