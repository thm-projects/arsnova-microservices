package de.thm.arsnova.shared.servicecommands

import java.util.UUID

import akka.Done
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import de.thm.arsnova.shared.entities.Session

object SessionCommands {
  import de.thm.arsnova.shared.mappings.SessionJsonProtocol._

  sealed trait SessionCommand extends ServiceCommand

  case class GetSession(id: UUID) extends SessionCommand

  object GetSessionFormat extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[GetSession] = jsonFormat1(GetSession)
  }

  case class CreateSession(session: Session) extends SessionCommand

  object CreateSessionFormat extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[CreateSession] = jsonFormat1(CreateSession)
  }

  case class GetSessionByKeyword(keyword: String) extends SessionCommand
}
