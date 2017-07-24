package de.thm.arsnova.shared

import java.util.UUID

import spray.json._

object Exceptions {
  case class NoUserException(methodName: String) extends Exception(methodName)
  case class NoSuchSession(reason: Either[UUID, String]) extends Exception
  case object NoSuchContent extends Exception {
    def toJson: JsString = JsString("No such Content")
  }
}
