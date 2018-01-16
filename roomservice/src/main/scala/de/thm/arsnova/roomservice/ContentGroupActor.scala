package de.thm.arsnova.roomservice

import java.util.UUID

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import de.thm.arsnova.shared.entities.export.ContentExport
import de.thm.arsnova.shared.servicecommands.ContentGroupCommands._
import de.thm.arsnova.shared.entities.{Content, ContentGroup, Room, User}
import de.thm.arsnova.shared.servicecommands.ContentCommands._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object ContentGroupActor {
  def props(contentRegion: ActorRef): Props =
    Props(new ContentGroupActor(contentRegion))
}

class ContentGroupActor(contentRegion: ActorRef) extends Actor {

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  var groups: collection.mutable.Map[String, ContentGroup] =
    collection.mutable.Map.empty[String, ContentGroup]

  def contentToType(content: Content): String = {
    content.format match {
      case "mc" => "choice"
      case "freetext" => "freetext"
    }
  }

  def getContentFromIds(ids: Seq[UUID]): Future[Seq[Content]] = {
    // TODO: Failure handling
    val contentListFutures: Seq[Future[Option[Content]]] = ids map { id =>
      (contentRegion ? GetContent(id)).mapTo[Try[Content]].map {
        case Success(content) => Some(content)
        case Failure(t) => None
      }
    }
    Future.sequence(contentListFutures).map { list =>
      list.flatten
    }
  }

  def receive: Receive = {
    case SetGroups(g) => {
      groups = collection.mutable.Map(g.toSeq: _*)
    }
    case AddToGroup(group: String, content: Content) => ((ret: ActorRef) => {
      groups.get(group) match {
        // add to existing group
        case Some(cg) => {
          if (cg.autoSort) {
            val contentList = Await.result(getContentFromIds(cg.contentIds), 5.seconds)
            val listWithNewContent = contentList :+ content
            val sorted = listWithNewContent.sortBy(c => (c.subject, c.content))
            val newCG = ContentGroup(true, sorted.map(_.id.get))
            groups.update(group, newCG)
            ret ! groups
          } else {
            val newCG = ContentGroup(false, cg.contentIds :+ content.id.get)
            groups.update(group, newCG)
            ret ! groups
          }
        }
        // create new group
        case None => {
          groups += group -> ContentGroup(true, Seq(content.id.get))
          ret ! groups
        }
      }
    }) (sender)
    case RemoveFromGroup(group, content) => ((ret: ActorRef) => {
      groups.get(group) match {
        case Some(cg) => {
          val newSeq = cg.contentIds.filterNot(_ == content.id.get)
          if (newSeq.isEmpty) {
            groups.remove(group)
          } else {
            groups.update(group, ContentGroup(cg.autoSort, newSeq))
          }
          ret ! groups
        }
      }
    }) (sender)
    case SendContent(ret, group) => {
      group match {
        case Some(g) => {
          groups.get(g) match {
            case Some(cg) => {
              getContentFromIds(cg.contentIds) pipeTo ret
            }
            case None => {
              ret ! Nil
            }
          }
        }
        case None => {
          val values: Seq[ContentGroup] = groups.values.map(identity).toSeq
          val cIds: Seq[UUID] = values.flatMap(_.contentIds)
          getContentFromIds(cIds) pipeTo ret
        }
      }
    }
    case GetExportList() => ((ret: ActorRef) => {
      val values: Seq[ContentGroup] = groups.values.map(identity).toSeq
      val cIds: Seq[UUID] = values.flatMap(_.contentIds)
      val contentListFutures: Seq[Future[Option[ContentExport]]] = cIds map { id =>
        (contentRegion ? GetExport(id)).mapTo[Try[ContentExport]].map {
          case Success(content) => Some(content)
          case Failure(t) => None
        }
      }
      Future.sequence(contentListFutures).map { list =>
        ret ! list.flatten
      }
    }) (sender)
  }
}
