package de.thm.arsnova.shared.entities

import java.util.UUID

case class Content(
  id: Option[UUID],
  roomId: UUID,
  subject: String,
  content: String,
  variant: String,
  group: String,
  hint: Option[String],
  solution: Option[String],
  active: Boolean,
  votingDisabled: Boolean,
  showStatistic: Boolean,
  showAnswer: Boolean,
  abstentionAllowed: Boolean,
  formatAttributes: Option[FormatAttributes],
  answerOptions: Option[Seq[AnswerOption]]
)