package de.thm.arsnova.shared

import java.util.UUID

import spray.json._

object Exceptions {
  trait ARSException extends Exception {
    def toJson: JsString
  }

  case class NoUserException(methodName: String) extends ARSException {
    def toJson: JsString = JsString("No user given")
  }
  case class NoSuchSession(reason: Either[UUID, String]) extends ARSException {
    def toJson: JsString = {
      val parsedReason = reason match {
        case Left(id) => s"Could not get session. So such id: $id"
        case Right(keyword) => s"Could not get session. So such keyword: $keyword"
      }
      JsString(parsedReason)
    }
  }
  case object NoSuchContent extends ARSException {
    def toJson: JsString = JsString("No such Content")
  }
  case class InsufficientRights(role: String, action: String) extends ARSException {
    def toJson: JsString = JsString(s"Insufficient rights for action $action with role: $role")
  }
}
