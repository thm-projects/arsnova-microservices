package de.thm.arsnova.shared.entities

import java.util.UUID

case class Question(
                     id: Option[UUID],
                     sessionId: UUID,
                     subject: String,
                     content: String,
                     variant: String,
                     format: String,
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