package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.actor.ActorRef

object KeywordCommands {

  sealed trait KeywordCommand extends ServiceCommand

  case class GetSessionEntry(keyword: String, ref: ActorRef) extends KeywordCommand

  case class GetSessionList(ref: ActorRef) extends KeywordCommand

  case class LookupSession(keyword: String) extends KeywordCommand

  case class SessionIdFromKeyword(id: Option[UUID]) extends KeywordCommand

  case object GenerateEntry extends KeywordCommand

  case class NewKeyword(keyword: String) extends KeywordCommand
}