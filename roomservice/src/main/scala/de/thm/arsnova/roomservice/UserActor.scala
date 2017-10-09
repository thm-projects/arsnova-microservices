package de.thm.arsnova.roomservice

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.persistence.PersistentActor
import akka.util.Timeout
import akka.cluster.sharding.ClusterSharding
import de.thm.arsnova.shared.Exceptions.{AddUserWentWrong, InvalidToken, ResourceNotFound}
import de.thm.arsnova.shared.entities.{Room, RoomRole, User, DbUser}
import de.thm.arsnova.shared.events.RoomEventPackage
import de.thm.arsnova.shared.events.RoomEvents.{RoomCreated, RoomDeleted}
import de.thm.arsnova.shared.events.UserEvents.{UserCreated, UserGetsRoomRole, UserLosesRoomRole}
import de.thm.arsnova.shared.servicecommands.AuthCommands.AddDbUser
import de.thm.arsnova.shared.servicecommands.RoomCommands._
import de.thm.arsnova.shared.servicecommands.UserCommands._
import de.thm.arsnova.shared.shards.RoomShard

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object UserActor {
  def props(authRouter: ActorRef): Props =
    Props(new UserActor(authRouter: ActorRef))
}

class UserActor(authRouter: ActorRef) extends PersistentActor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val roomRegion = ClusterSharding(context.system).shardRegion(RoomShard.shardName)

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // passivate the entity when no activity
  context.setReceiveTimeout(2.minutes)

  private var userState: Option[User] = None
  private val rolesState: collection.mutable.Set[RoomRole] = collection.mutable.Set.empty[RoomRole]

  override def receiveRecover: Receive = {
    case UserCreated(user) =>
      userState = Some(user)
      context.become(userCreated)
    case UserGetsRoomRole(role) =>
      rolesState += role
    case UserLosesRoomRole(role) =>
      rolesState -= role
  }

  override def receiveCommand: Receive = initial

  def handleRoomEvents(sep: RoomEventPackage) = {
    sep.event match {
      case RoomCreated(room) => {
        val newRole = RoomRole(room.userId.get, room.id.get, "owner")
        rolesState += newRole
        persist(UserGetsRoomRole(newRole)) { e => e}
      }
      case RoomDeleted(room) => {
        val oldRole = RoomRole(room.userId.get, room.id.get, "owner")
        rolesState -= oldRole
        persist(UserLosesRoomRole(oldRole))(e => e)
      }
    }
  }

  def initial: Receive = {
    case CreateUser(userId, user) => ((ret: ActorRef) => {
      (authRouter ? AddDbUser(DbUser(Some(userId), user.username, user.password))).mapTo[Int] map {
        // User must be added to auth db for login
        // authRouter answers with tables touched
        // TODO: maybe return bool - but it would add delay since authRouter can't pipe
        case 1 => {
          ret ! Success(user)
          userState = Some(user)
          context.become(userCreated)
          persist(UserCreated(user))(e => e)
        }
        case _ => {
          ret ! Failure(AddUserWentWrong(user.username))
        }
      }
    }) (sender)
    case GetUser(userId) => {
      sender() ! Failure(ResourceNotFound("user"))
    }
  }

  def userCreated: Receive = {
    case GetUser(userId) => ((ret: ActorRef) => {
      userState match {
        case Some(u) => ret ! Success(u)
        case None => ret ! Failure(ResourceNotFound("user"))
      }
    }) (sender)
    case GetRoleForRoom(userId, roomId) => ((ret: ActorRef) => {
      // need to ensure that there is only one role set per user to use find and not filter
      rolesState.find(r => r.userId == userId && r.roomId == roomId) match {
        case Some(r) => ret ! r.role
        case None => ret ! "guest"
      }
    }) (sender)
    case GetUserRooms(userId, withRole) => ((ret: ActorRef) => {
      val roles: Seq[RoomRole] = withRole match {
        case Some(r) => {
          rolesState.filter(_.role == r).toSeq
        }
        case None => {
          rolesState.toSeq
        }
      }
      val askFutures: Seq[Future[Option[Room]]] = roles map { sr =>
        (roomRegion ? GetRoom(sr.roomId)).mapTo[Try[Room]].map {
          case Success(room) => Some(room)
          case Failure(t) => {
            // TODO: handle dead entries in roles set / Exceptions?
            None
          }
        }
      }
      Future.sequence(askFutures).map { list =>
        ret ! Success(list.flatten)
      }
    }) (sender)
    case sep: RoomEventPackage => handleRoomEvents(sep)
  }
}
