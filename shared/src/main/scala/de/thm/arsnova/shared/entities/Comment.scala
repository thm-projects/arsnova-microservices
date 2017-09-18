package de.thm.arsnova.shared.entities

import java.util.UUID

case class Comment(id: Option[UUID], userId: UUID, roomId: UUID, isRead: Boolean, subject: String, text: String, createdAt: String)
