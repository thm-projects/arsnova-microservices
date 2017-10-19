package de.thm.arsnova.shared.entities.export

import java.util.UUID
import de.thm.arsnova.shared.entities.FormatAttributes

case class ContentExport(
  id: UUID,
  subject: String,
  content: String,
  format: String,
  group: String,
  hint: Option[String],
  solution: Option[String],
  active: Boolean,
  votingDisabled: Boolean,
  showStatistic: Boolean,
  showAnswer: Boolean,
  abstentionAllowed: Boolean,
  formatAttributes: Option[FormatAttributes],
  answerOptions: Option[Seq[AnswerOptionExport]],
  answers: Option[Seq[FreetextAnswerExport]]
)
