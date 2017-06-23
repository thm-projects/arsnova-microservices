package de.thm.arsnova.managementservice

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import akka.actor.{Actor, ActorRef, RootActorPath, Address}
import akka.pattern.pipe
import de.thm.arsnova.shared.management.RegistryCommands._

class ServiceRegistryActor extends Actor {
  val cluster = Cluster(context.system)

  implicit val ec: ExecutionContext = context.system.dispatcher

  val remoteCommander = context.actorSelection("akka://CommandService@127.0.0.1:8880/user/router")

  // managing all nodes of the cluster
  var nodes = scala.collection.mutable.HashSet[Member]()
  // mangaing all actors that receive (arsnova-)commands
  val serviceActors = collection.mutable.Map[String, ActorRef]()

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {
    // a new member joins the cluster
    case MemberUp(newMember) => {
      ARSnovaCluster.addMember(newMember)
    }
    // a member leaves the cluster
    case MemberLeft(member) => {
      ARSnovaCluster.removeMember(member)
    }

    // a service registeres an actor for handling service commands
    case RegisterService(serviceType, remote) => {
      ARSnovaCluster.addServiceActor(sender.path.address, serviceType, remote)
      remoteCommander ! RegisterService(serviceType, remote)
    }
    case UnregisterService(serviceType, remote) => {
      ARSnovaCluster.removeServiceActor(sender.path.address, serviceType) match {
        // there is an actual serviceActor
        case Some(ref) =>
        // no serviceActor (service hasn't registered any) :(
        case None =>
      }
    }
  }
}
