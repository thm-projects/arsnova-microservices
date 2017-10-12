package de.thm.arsnova.shared.servicecommands

import akka.actor.ActorRef
import de.thm.arsnova.shared.entities.Content

object ContentGroupCommands {
 sealed trait ContentGroupCommand extends ServiceCommand

  case class AddToGroup(group: String, content: Content)

  case class RemoveFromGroup(group: String, content: Content)

  case class SendContent(ret: ActorRef, group: Option[String])
}
