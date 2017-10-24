package de.thm.arsnova.shared.entities

import java.util.UUID

case class AnswerOption(
  index: Int,
  contentId: Option[UUID],
  correct: Boolean,
  text: String,
  value: Int
)
