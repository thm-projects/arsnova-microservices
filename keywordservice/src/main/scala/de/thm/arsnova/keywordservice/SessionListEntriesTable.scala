package de.thm.arsnova.keywordservice

import java.util.UUID
import slick.driver.PostgresDriver.api._
import slick.sql.SqlProfile.ColumnOption._

import de.thm.arsnova.shared.entities.SessionListEntry

class SessionListEntriesTable(tag: Tag) extends Table[SessionListEntry](tag, "sessionlistentries"){
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def key: Rep[String] = column[String]("keyword")

  def * = (id, key) <> (SessionListEntry.tupled, SessionListEntry.unapply)
}
