package de.thm.arsnova.keywordservice

import java.util.UUID

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery
import de.thm.arsnova.shared.entities.SessionListEntry

object SessionListEntryRepository {
  import Context._

  val KEYLENGTH = 8

  val db: Database = Database.forConfig("database")
  val sessionListEntriesTable = TableQuery[SessionListEntriesTable]

  def generateNewEntry: SessionListEntry = {
    val intList = for (i <- 1 to KEYLENGTH) yield Random.nextInt(10)
    val keyword = intList.map(_.toString).mkString("")
    val id = UUID.randomUUID()
    SessionListEntry(id, keyword)
  }

  // this is somewhat bruteforce because it generates both uuid and key new if insert fails
  def create(tries: Int = 0): Option[SessionListEntry] = {
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

  def insert(s: SessionListEntry): Future[Boolean] = {
    val qry = sessionListEntriesTable += s
    db.run(qry).map {
      case 0 => false
      case 1 => true
    }
  }

  def getEntryFromId(keyword: String): Future[Option[SessionListEntry]] = {
    val qry = sessionListEntriesTable.filter(_.key === keyword).result.headOption
    db.run(qry)
  }
}
