package de.thm.arsnova.shared

import java.util.UUID

object Exceptions {
  case class NoUserException(methodName: String) extends Exception(methodName)
  case class NoSuchSession(reason: Either[UUID, String]) extends Exception
}
