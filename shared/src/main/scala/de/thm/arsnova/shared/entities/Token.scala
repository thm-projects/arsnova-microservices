package de.thm.arsnova.shared.entities

import java.util.UUID

case class Token(token: String, userId: UUID, created: String, modified: Option[String], lastUsed: String)