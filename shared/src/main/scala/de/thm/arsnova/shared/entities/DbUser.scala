package de.thm.arsnova.shared.entities

import java.util.UUID

case class DbUser(
  id: Option[UUID],
  username: String,
  password: String
)