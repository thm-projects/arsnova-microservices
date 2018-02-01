package de.thm.arsnova.shared.entities

import java.util.UUID

import de.thm.arsnova.shared.entities.export.ContentExport

case class Content(
  id: Option[UUID],
  roomId: UUID,
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
  votingRound: Int,
  formatAttributes: Option[FormatAttributes],
  answerOptions: Option[Seq[AnswerOption]]
)

object Content {
  def apply(c: ContentExport, roomId: UUID): Content =
    Content(
      None,
      roomId,
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
      0,
      c.formatAttributes,
      c.answerOptions
    )
}