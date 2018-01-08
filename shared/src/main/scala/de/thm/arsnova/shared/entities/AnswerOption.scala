package de.thm.arsnova.shared.entities

import java.util.UUID

import de.thm.arsnova.shared.entities.export.AnswerOptionExport

case class AnswerOption(
  index: Int,
  contentId: Option[UUID],
  correct: Boolean,
  text: String,
  value: Int
)

object AnswerOption {
  def apply(a: AnswerOptionExport, index: Int, contentId: UUID): AnswerOption =
    AnswerOption(
      index,
      Some(contentId),
      a.correct,
      a.text,
      a.value
    )
}
