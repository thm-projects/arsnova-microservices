package de.thm.arsnova.shared

object Exceptions {
  case class NoUserException(methodName: String) extends Exception(methodName)
  case class NoSuchSession(reason: String) extends Exception(reason)
}
