package de.thm.arsnova.testdata

import java.util.UUID

import de.thm.arsnova.shared.entities._

object SpeakerDataGenerator {
  def gen(count: Int, ownerId: UUID): Seq[(Room, Seq[Content])] = {
    val sessionId = UUID.randomUUID()
    for(i <- 0 to count) {
      val room = Room(Some(sessionId), None, Some(ownerId), )
    }
  }

  def genContentGroups(count: Int): Seq[ContentGroup] = {
    for (i <- 0 to count) {
      
    }
  }

  def genContent(count: Int): Seq[Content] = {

  }
}
