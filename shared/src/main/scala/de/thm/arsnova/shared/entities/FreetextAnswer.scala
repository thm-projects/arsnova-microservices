package de.thm.arsnova.shared.entities

import java.util.UUID

case class FreetextAnswer(id: Option[UUID], questionId: UUID, sessionId: UUID, subject: String, text: String)