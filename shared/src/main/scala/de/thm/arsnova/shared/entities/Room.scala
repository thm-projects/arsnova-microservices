package de.thm.arsnova.shared.entities

import java.util.UUID

case class Room(
  id: Option[UUID],
  keyword: Option[String],
  userId: Option[UUID],
  contentGroups: Map[String, ContentGroup],
  title: String,
  shortName: String,
  lastOwnerActivity: String,
  creationTime: String,
  active: Boolean,
  feedbackLock: Boolean,
  flipFlashcards: Boolean
)
