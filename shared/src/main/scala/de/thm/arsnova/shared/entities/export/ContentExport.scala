package de.thm.arsnova.shared.entities.export

import java.util.UUID
import de.thm.arsnova.shared.entities.{Content, FormatAttributes}

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

object ContentExport {
  def apply(c: Content): ContentExport =
    ContentExport(
      c.id.get,
      c.subject,
      c.content,
      c.format,
      c.group,
      c.hint,
      c.solution,
      c.active,
      c.votingDisabled,
      c.showStatistic,
      c.showAnswer,
      c.abstentionAllowed,
      c.formatAttributes,
      None,
      None
    )
}
