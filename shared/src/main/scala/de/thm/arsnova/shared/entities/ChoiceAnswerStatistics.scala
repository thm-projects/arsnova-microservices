package de.thm.arsnova.shared.entities

case class ChoiceAnswerStatistics(choices: Seq[ChoiceAnswerSummary], abstentions: Seq[Int])

case class ChoiceAnswerSummary(choice: Seq[Int], count: Int)
