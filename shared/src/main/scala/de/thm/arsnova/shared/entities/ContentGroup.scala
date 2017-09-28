package de.thm.arsnova.shared.entities

import java.util.UUID

case class ContentGroup(
  autoSort: Boolean,
  contentIds: Seq[UUID]
)
