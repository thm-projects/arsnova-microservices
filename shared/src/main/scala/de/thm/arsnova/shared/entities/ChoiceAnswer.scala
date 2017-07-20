package de.thm.arsnova.shared.entities

import java.util.UUID

case class ChoiceAnswer(id: Option[UUID], contentId: UUID, sessionId: UUID, answerOptionId: UUID)
