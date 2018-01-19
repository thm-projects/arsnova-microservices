package de.thm.arsnova.shared.entities

import java.util.UUID

case class ChoiceAnswer(
  id: Option[UUID],
  userId: Option[UUID],
  contentId: Option[UUID],
  roomId: Option[UUID],
  answerIndexes: Option[Seq[Int]],
  round: Option[Int],
  abstention: Boolean
)
