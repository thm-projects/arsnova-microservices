package de.thm.arsnova.shared.entities.export

import de.thm.arsnova.shared.entities.{ChoiceAnswerSummary, RoundTransition}

case class ChoiceAnswerExport(
  stats: Option[Seq[ChoiceAnswerSummary]],
  transitions: Option[Seq[RoundTransition]]
)
