package de.thm.arsnova.shared.entities

import java.util.UUID

case class FreetextAnswer(
  id: Option[UUID],
  userId: Option[UUID],
  contentId: Option[UUID],
  roomId: Option[UUID],
  subject: String,
  text: String
)
