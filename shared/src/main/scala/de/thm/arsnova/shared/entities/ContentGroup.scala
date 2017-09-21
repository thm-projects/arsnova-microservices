package de.thm.arsnova.shared.entities

import java.util.UUID

case class ContentGroup(
  id: UUID,
  name: String,
  autoSort: Boolean,
  contentIds: Seq[UUID]
)
