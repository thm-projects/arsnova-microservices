package de.thm.arsnova.shared

import java.util.UUID

import spray.json._

object Exceptions {
  trait ARSException extends Exception {
    def getMsg: String
  }

  case class ResourceNotFound(resource: String) extends ARSException {
    def getMsg: String = s"Resource $resource could not be found"
  }

  case class NoUserException(methodName: String) extends ARSException {
    def getMsg: String = "No user given"
  }
  case class NoSuchRoom(reason: Either[UUID, String]) extends ARSException {
    def getMsg: String = {
      reason match {
        case Left(id) => s"Could not get room. So such id: $id"
        case Right(keyword) => s"Could not get room. So such keyword: $keyword"
      }
    }
  }

  case class InsufficientRights(role: String, action: String) extends ARSException {
    def getMsg: String = s"Insufficient rights for action $action with role: $role"
  }
  case class InvalidToken(token: String) extends ARSException {
    def getMsg: String = s"Invalid token: $token"
  }
  case class AddUserWentWrong(username: String) extends ARSException {
    def getMsg: String = s"User with username $username couldn't be added to database"
  }
}
