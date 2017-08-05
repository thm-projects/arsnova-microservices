package de.thm.arsnova.shared.events

import java.util.UUID

case class SessionEventPackage(id: UUID, event: ServiceEvent)
