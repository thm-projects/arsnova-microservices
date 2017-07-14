package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import akka.actor.ActorRef
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.Session

object SessionCommands {
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  sealed trait SessionCommand extends ServiceCommand {
    def id: UUID
  }

  case class GetSession(id: UUID) extends SessionCommand

  object GetSessionFormat extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[GetSession] = jsonFormat1(GetSession)
  }

  case class CreateSession(id: UUID, session: Session, token: Option[String]) extends SessionCommand

  object CreateSessionFormat extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[CreateSession] = jsonFormat3(CreateSession)
  }

  sealed trait SessionListCommand extends ServiceCommand

  case class GetSessionList(ref: ActorRef) extends SessionListCommand

  case class SessionList(list: Seq[SessionListEntry]) extends SessionListCommand

  case class SessionListEntry(id: UUID, keyword: String) extends SessionListCommand

  case class LookupSession(keyword: String) extends SessionListCommand

  case class SessionIdFromKeyword(id: Option[UUID]) extends SessionListCommand

  case class GenerateKeyword(id: UUID) extends SessionListCommand

  case class NewKeyword(keyword: String) extends SessionListCommand
}
