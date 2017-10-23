package de.thm.arsnova.shared.entities.export

import de.thm.arsnova.shared.entities.AnswerOption

case class AnswerOptionExport(
  correct: Boolean,
  text: String,
  value: Int,
  count: Int
)

object AnswerOptionExport {
  def apply(a: AnswerOption, c: Int): AnswerOptionExport =
    AnswerOptionExport(
      a.correct,
      a.text,
      a.value,
      c
    )
}
