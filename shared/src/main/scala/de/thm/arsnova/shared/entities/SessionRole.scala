package de.thm.arsnova.shared.entities

import java.util.UUID

case class SessionRole(userId: UUID, sessionId: UUID, role: String)
