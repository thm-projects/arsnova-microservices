package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.actor.ActorRef

object KeywordCommands {

  sealed trait KeywordCommand extends ServiceCommand

  case class GetRoomEntry(keyword: String, ref: ActorRef) extends KeywordCommand

  case class GetRoomList(ref: ActorRef) extends KeywordCommand

  case class LookupRoom(keyword: String) extends KeywordCommand

  case class RoomIdFromKeyword(id: Option[UUID]) extends KeywordCommand

  case object GenerateEntry extends KeywordCommand

  case class NewKeyword(keyword: String) extends KeywordCommand
}