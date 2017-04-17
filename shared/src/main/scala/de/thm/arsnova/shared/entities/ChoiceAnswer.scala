package de.thm.arsnova.shared.entities

import java.util.UUID

case class ChoiceAnswer(id: Option[UUID], questionId: UUID, sessionId: UUID, answerOptionId: UUID)
