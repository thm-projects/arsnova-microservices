package de.thm.arsnova.keywordservice

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.sql.SqlProfile.ColumnOption._

import de.thm.arsnova.shared.entities.RoomListEntry

class RoomListEntriesTable(tag: Tag) extends Table[RoomListEntry](tag, "roomlistentries"){
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def key: Rep[String] = column[String]("keyword")

  def * = (id, key) <> (RoomListEntry.tupled, RoomListEntry.unapply)
}
