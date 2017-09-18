package de.thm.arsnova.keywordservice

import java.util.UUID

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery
import de.thm.arsnova.shared.entities.RoomListEntry

object RoomListEntryRepository {
  import Context._

  val KEYLENGTH = 8

  val db: Database = Database.forConfig("database")
  val roomListEntriesTable = TableQuery[RoomListEntriesTable]

  def generateNewEntry: RoomListEntry = {
    val intList = for (i <- 1 to KEYLENGTH) yield Random.nextInt(10)
    val keyword = intList.map(_.toString).mkString("")
    val id = UUID.randomUUID()
    RoomListEntry(id, keyword)
  }

  // this is somewhat bruteforce because it generates both uuid and key new if insert fails
  def create(tries: Int = 0): Option[RoomListEntry] = {
    if (tries < 5) {
      val newEntry = generateNewEntry
      Await.result(insert(newEntry), 5.seconds) match {
        case true => Some(newEntry)
        case false => create(tries + 1)
      }
    } else {
      None
    }
  }

  def insert(s: RoomListEntry): Future[Boolean] = {
    val qry = roomListEntriesTable += s
    db.run(qry).map {
      case 0 => false
      case 1 => true
    }
  }

  def getEntryFromId(keyword: String): Future[Option[RoomListEntry]] = {
    val qry = roomListEntriesTable.filter(_.key === keyword).result.headOption
    db.run(qry)
  }
}
