package de.thm.arsnova.shared.entities.export

case class AnswerOptionExport(
  correct: Boolean,
  text: String,
  value: Int,
  count: Int
)
