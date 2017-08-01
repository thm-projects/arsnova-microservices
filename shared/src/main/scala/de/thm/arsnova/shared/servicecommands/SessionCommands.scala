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

  case class CreateSession(id: UUID, session: Session, token: String) extends SessionCommand

  object CreateSessionFormat extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[CreateSession] = jsonFormat3(CreateSession)
  }

  case class UpdateSession(id: UUID, session: Session, token: String) extends SessionCommand

  case class DeleteSession(id: UUID, token: String) extends SessionCommand
}
