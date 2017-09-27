package de.thm.arsnova.shared.entities

import java.util.UUID

case class ContentGroup(
  name: String,
  autoSort: Boolean,
  contentIds: Seq[UUID]
)
