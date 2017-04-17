package de.thm.arsnova.shared.entities

import java.util.UUID

case class AnswerOption(id: Option[UUID], questionId: Option[UUID], correct: Boolean, text: String, value: Int)
