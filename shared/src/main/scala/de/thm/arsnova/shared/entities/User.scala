package de.thm.arsnova.shared.entities

import java.util.UUID

case class User(
  id: Option[UUID],
  username: String,
  password: String
)