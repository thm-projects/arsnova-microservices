package de.thm.arsnova.shared.entities


case class RoundTransition(
  roundA: Int,
  roundB: Int,
  selectedIndexesA: Seq[Int],
  selectedIndexesB: Seq[Int],
  count: Int
)
