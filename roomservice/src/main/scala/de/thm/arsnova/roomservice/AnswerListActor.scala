package de.thm.arsnova.roomservice

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.persistence.PersistentActor
import akka.util.Timeout
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.shared.Exceptions.{InsufficientRights, ResourceNotFound}
import de.thm.arsnova.shared.entities._
import de.thm.arsnova.shared.entities.export.FreetextAnswerExport
import de.thm.arsnova.shared.events.ChoiceAnswerEvents._
import de.thm.arsnova.shared.events.ContentEvents._
import de.thm.arsnova.shared.events.FreetextAnswerEvents._
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.global.GuestUser
import de.thm.arsnova.shared.servicecommands.ChoiceAnswerCommands._
import de.thm.arsnova.shared.servicecommands.ContentCommands._
import de.thm.arsnova.shared.servicecommands.FreetextAnswerCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands.GetRoleForRoom
import de.thm.arsnova.shared.shards.{ContentShard, EventShard, UserShard}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object AnswerListActor {
  def props(authRouter: ActorRef): Props =
    Props(new AnswerListActor(authRouter: ActorRef))
}

class AnswerListActor(authRouter: ActorRef) extends PersistentActor {

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val eventRegion = ClusterSharding(context.system).shardRegion(EventShard.shardName)

  val userRegion = ClusterSharding(context.system).shardRegion(UserShard.shardName)

  val contentRegion = ClusterSharding(context.system).shardRegion(ContentShard.shardName)

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  var answerOptions: Option[Seq[AnswerOption]] = None

  var votingRound: Int = 0

  private val choiceAnswerList: collection.mutable.HashMap[UUID, ChoiceAnswer] =
    collection.mutable.HashMap.empty[UUID, ChoiceAnswer]
  private val freetextAnswerList: collection.mutable.HashMap[UUID, FreetextAnswer] =
    collection.mutable.HashMap.empty[UUID, FreetextAnswer]

  override def receiveRecover: Receive = {
    case ContentCreated(content) => {
      contentToType(content) match {
        case "choice" => {
          context.become(choiceContentCreated)
          answerOptions = content.answerOptions
        }
        case "freetext" => {
          context.become(freetextContentCreated)
        }
      }
    }
    case ContentDeleted(content) => {
      context.become(initial)
      choiceAnswerList.clear()
      freetextAnswerList.clear()
      answerOptions = None
    }
    case NewRound(contentId, round) => {
      votingRound = round
    }

    case ChoiceAnswerCreated(answer) => {
      choiceAnswerList += answer.id.get -> answer
    }
    case ChoiceAnswersCreated(answers) => {
      answers.map { answer =>
        choiceAnswerList += answer.id.get -> answer
      }
    }
    case ChoiceAnswerDeleted(answer) => {
      choiceAnswerList -= answer.id.get
    }

    case FreetextAnswerCreated(answer) => {
      freetextAnswerList += answer.id.get -> answer
    }
    case FreetextAnswersCreated(answers) => {
      answers.map { answer =>
        freetextAnswerList += answer.id.get -> answer
      }
    }
    case FreetextAnswerDeleted(answer) => {
      freetextAnswerList -= answer.id.get
    }
  }

  override def receiveCommand: Receive = initial

  def handleEvents(sep: RoomEventPackage) = {
    sep.event match {
      case ContentCreated(content) => {
        contentToType(content) match {
          case "choice" => {
            context.become(choiceContentCreated)
            answerOptions = content.answerOptions
          }
          case "freetext" => {
            context.become(freetextContentCreated)
          }
        }
        persist(ContentCreated(content))(e => e)
      }
      case ContentDeleted(content) => {
        choiceAnswerList.clear()
        freetextAnswerList.clear()
        answerOptions = None
        persist(ContentDeleted(content))(e => e)
      }
      case e@NewRound(contentId, round) => {
        votingRound = round
        persist(e)(e => e)
      }
    }
  }

  def contentToType(content: Content): String = {
    content.format match {
      case "mc" => "choice"
      case "freetext" => "freetext"
    }
  }

  def initial: Receive = {
    case sep: RoomEventPackage => handleEvents(sep)
    case ImportChoiceAnswers(contentId, roomId, content, choiceAnswerExport, abstentionCount) => {
      val answers = collection.mutable.Seq.empty[ChoiceAnswer]
      abstentionCount.foreach { a =>
        for (i <- 0 to a) {
          val newId = UUID.randomUUID()
          answers :+ ChoiceAnswer(Some(newId), Some(GuestUser().id.get), Some(contentId), Some(roomId), Nil, Some(i))
        }
      }
      choiceAnswerExport.transitions match {
        case Some(transitions) =>
          // Content has more than one round, don't use stats to import
          transitions.foreach { transition =>
            if (votingRound != transition.roundA) {
              votingRound = transition.roundA
            }
            for (i <- 0 to transition.count) {
              val newId = UUID.randomUUID()
              answers :+ ChoiceAnswer(Some(newId), Some(GuestUser().id.get), Some(contentId), Some(roomId), transition.selectedIndexesA, Some(votingRound))
            }
          }
          // since only stats for roundA got imported, import for the last round
          transitions.filter(t => t.roundA == votingRound).foreach { transition =>
            val newId = UUID.randomUUID()
            answers :+ ChoiceAnswer(Some(newId), Some(GuestUser().id.get), Some(contentId), Some(roomId), transition.selectedIndexesB, Some(votingRound))
          }
          votingRound = votingRound + 1
        case None =>
          // Try to import from stats
          choiceAnswerExport.stats match {
            case Some(stats) =>
              stats.foreach { summary =>
                for (i <- 0 to summary.count) {
                  val newId = UUID.randomUUID()
                  answers :+ ChoiceAnswer(Some(newId), Some(GuestUser().id.get), Some(contentId), Some(roomId), summary.choice, Some(votingRound))
                }
              }
          }
      }
      // save all answers
      answers.map { answer =>
        choiceAnswerList += answer.id.get -> answer
      }
      // persist events
      persist(ContentCreated(content))(e => e)
      persist(ChoiceAnswersCreated(answers))(e => e)
      
      answerOptions = Some(content.answerOptions.get.map(a => a.copy(contentId = Some(contentId))))
      context.become(choiceContentCreated)
    }
    case ImportFreetextAnswers(contentId, roomId, content, exportedAnswers) => {
      val answers = collection.mutable.Seq.empty[FreetextAnswer]
      exportedAnswers.map { eAnswer =>
        val newId = UUID.randomUUID()
        val guestUser = GuestUser()
        val answer = FreetextAnswer(Some(newId), guestUser.id, Some(contentId), Some(roomId), eAnswer.subject, eAnswer.text)
        answers :+ answer
        freetextAnswerList += newId -> answer
        persistAsync(FreetextAnswerCreated(answer))(e => e)
      }
      // persist events
      persist(ContentCreated(content))(e => e)
      persist(FreetextAnswersCreated(answers))(e => e)

      context.become(freetextContentCreated)
    }
  }

  def choiceContentCreated: Receive = {
    case sep: RoomEventPackage => handleEvents(sep)
    case GetChoiceAnswers(contentId) => {
      sender() ! Success(choiceAnswerList.values.map(identity).toSeq)
    }
    case GetChoiceAnswer(contentId, id) => {
      choiceAnswerList.get(id) match {
        case Some(a) => sender() ! Success(a)
        case None => sender() ! ResourceNotFound("choice answer")
      }
    }
    case CreateChoiceAnswer(contentId, roomId, answer, userId) => ((ret: ActorRef) => {
      val newId = UUID.randomUUID()
      val awu = answer.copy(id = Some(newId), userId = Some(userId), roomId = Some(roomId), contentId = Some(contentId), round = Some(votingRound))
      ret ! Success(awu)
      eventRegion ! RoomEventPackage(roomId, ChoiceAnswerCreated(awu))
      choiceAnswerList += awu.id.get -> awu
      persist(ChoiceAnswerCreated(awu)) { e => e }
    }) (sender)
    case cmd@DeleteChoiceAnswer(contentId, roomId, id, userId) => ((ret: ActorRef) => {
      choiceAnswerList.get(id) match {
        case Some(a) => {
          if (a.userId.get == userId) {
            choiceAnswerList -= id
            eventRegion ! RoomEventPackage(a.roomId.get, ChoiceAnswerDeleted(a))
            ret ! Success(a)
            persist(ChoiceAnswerDeleted(a)) { e => e }
          } else {
            (userRegion ? GetRoleForRoom(userId, roomId)).mapTo[String] map { role =>
              ChoiceAnswerCommandWithRole(cmd, role, ret)
            } pipeTo self
          }
        }
        case None => {
          ret ! ResourceNotFound(s"choice answer $id")
        }
      }
    }) (sender)
    case GetChoiceStatistics(contentId) => ((ret: ActorRef) => {
      val list = choiceAnswerList.values.map(identity).toSeq
      val abs: Array[Int] = new Array[Int](votingRound)
      val c: Set[Seq[Int]] = list.map { a =>
        if (a.answerIndexes.isEmpty) {
          abs.update(a.round.get, abs(a.round.get) + 1)
          Nil
        } else {
          a.answerIndexes
        }
      }.toSet
      val choices = c.map { choice =>
        ChoiceAnswerSummary(choice, c.count(_ == choice))
      }.toSeq
      ret ! ChoiceAnswerStatistics(choices, abs.toSeq)
    }) (sender)
    case GetChoiceAbstentionCount(contentId) => ((ret: ActorRef) => {
      val list = choiceAnswerList.values.map(identity).toSeq
      val abs: Array[Int] = new Array[Int](votingRound)
      list.foreach { a =>
        if (a.answerIndexes.isEmpty) {
           abs.update(a.round.get, abs(a.round.get) + 1)
          Nil
        } else {
          a.answerIndexes
        }
      }
      ret ! abs.toSeq
    }) (sender)
    case GetSummary(contentId) => ((ret: ActorRef) => {
      val list = choiceAnswerList.values.map(identity).toSeq
      val c: Set[Seq[Int]] = list.map { a =>
        if (a.answerIndexes.isEmpty) {
          Nil
        } else {
          a.answerIndexes
        }
      }.toSet
      val choices = c.map { choice =>
        ChoiceAnswerSummary(choice, c.count(_ == choice))
      }.toSeq
      ret ! choices
    }) (sender)

    case GetTransitions(contentId, roundA, roundB) => ((ret: ActorRef) => {
      var transitions: Seq[RoundTransition] = Seq.empty[RoundTransition]
      choiceAnswerList.values.foreach( (answer) => {
        if (answer.round.get == roundA) {
          val second = choiceAnswerList.values.find(a => (a.userId.get == answer.userId.get) && (a.round.get == roundB))
          second match {
            // user has given an answer for roundB
            case Some(secondAnswer) => {
              var updated = false
              transitions = transitions.map( t => {
                if ((t.selectedIndexesA == answer.answerIndexes) && (t.selectedIndexesB == secondAnswer.answerIndexes)) {
                  updated = true
                  RoundTransition(roundA, roundB, t.selectedIndexesA, t.selectedIndexesB, t.count + 1)
                } else {
                  t
                }
              })
              if (!updated) {
                transitions = transitions :+ RoundTransition(roundA, roundB, answer.answerIndexes, secondAnswer.answerIndexes, 1)
              }
            }
          }
        }
      })
      ret ! Success(transitions)
    }) (sender)
    case GetAllTransitions(contentId) => ((ret: ActorRef) => {
      var transitions: Seq[RoundTransition] = Seq.empty[RoundTransition]
      for (i <- 0 to votingRound -1) {
        val roundA = i
        val roundB = i + 1
        choiceAnswerList.values.foreach((answer) => {
          if (answer.round.get == roundA) {
            val second = choiceAnswerList.values.find(a => (a.userId.get == answer.userId.get) && (a.round.get == roundB))
            second match {
              // user has given an answer for roundB
              case Some(secondAnswer) => {
                var updated = false
                transitions = transitions.map(t => {
                  if ((t.selectedIndexesA == answer.answerIndexes) && (t.selectedIndexesB == secondAnswer.answerIndexes)) {
                    updated = true
                    RoundTransition(roundA, roundB, t.selectedIndexesA, t.selectedIndexesB, t.count + 1)
                  } else {
                    t
                  }
                })
                if (!updated) {
                  transitions = transitions :+ RoundTransition(roundA, roundB, answer.answerIndexes, secondAnswer.answerIndexes, 1)
                }
              }
            }
          }
        })
      }
      ret ! Success(transitions)
    }) (sender)

    case ChoiceAnswerCommandWithRole(cmd, role, ret) => {
      cmd match {
        case DeleteChoiceAnswer(roomId, contentId, id, userId) => {
          choiceAnswerList.get(id) match {
            case Some(a) => {
              if (role == "owner") {
                choiceAnswerList -= id
                eventRegion ! RoomEventPackage(a.roomId.get, ChoiceAnswerDeleted(a))
                ret ! Success(a)
                persist(ChoiceAnswerDeleted(a)) { e => e }
              } else {
                ret ! Failure(InsufficientRights(role, "DeleteChoiceAnswer"))
              }
            }
          }
        }
      }
    }
  }

  def freetextContentCreated: Receive = {
    case sep: RoomEventPackage => handleEvents(sep)
    case GetFreetextAnswers(contentId) => {
      sender() ! freetextAnswerList.values.map(identity).toSeq
    }
    case GetFreetextAnswer(contentId, id) => {
      sender() ! freetextAnswerList.get(id)
    }
    case CreateFreetextAnswer(roomId, contentId, answer, userId) => ((ret: ActorRef) => {
      val newId = UUID.randomUUID()
      val awu = answer.copy(id = Some(newId), userId = Some(userId), roomId = Some(roomId), contentId = Some(contentId))
      ret ! Success(awu)
      eventRegion ! RoomEventPackage(roomId, FreetextAnswerCreated(awu))
      freetextAnswerList += awu.id.get -> awu
      persist(FreetextAnswerCreated(awu)) { e => e }
    }) (sender)
    case cmd@DeleteFreetextAnswer(roomId, contentId, id, userId) => ((ret: ActorRef) => {
      freetextAnswerList.get(id) match {
        case Some(a) => {
          if (a.userId.get == userId) {
            freetextAnswerList -= id
            eventRegion ! RoomEventPackage(a.roomId.get, FreetextAnswerDeleted(a))
            ret ! Success(a)
            persist(FreetextAnswerDeleted(a)) { e => e }
          } else {
            (userRegion ? GetRoleForRoom(userId, roomId)).mapTo[String] map { role =>
              FreetextAnswerCommandWithRole(cmd, role, ret)
            } pipeTo self
          }
        }
        case None => {
          ret ! ResourceNotFound(s"choice answer $id")
        }
      }
    }) (sender)
    case GetFreetextStatistics(contentId) => ((ret: ActorRef) => {
      val list = freetextAnswerList.values.map(identity).toSeq
      ret ! list.map(FreetextAnswerExport(_))
    }) (sender)

    case FreetextAnswerCommandWithRole(cmd, role, ret) => {
      cmd match {
        case DeleteFreetextAnswer(roomId, contentId, id, userId) => {
          freetextAnswerList.get(id) match {
            case Some(a) => {
              if (role == "owner") {
                freetextAnswerList -= id
                eventRegion ! RoomEventPackage(a.roomId.get, FreetextAnswerDeleted(a))
                ret ! Success(a)
                persist(FreetextAnswerDeleted(a)) { e => e }
              } else {
                ret ! Failure(InsufficientRights(role, "DeleteChoiceAnswer"))
              }
            }
          }
        }
      }
    }
  }
}
