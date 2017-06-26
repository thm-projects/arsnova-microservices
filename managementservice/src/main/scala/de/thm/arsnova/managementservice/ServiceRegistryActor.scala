package de.thm.arsnova.managementservice

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import akka.actor.{Actor, ActorRef, Address, RootActorPath}
import akka.pattern.pipe
import de.thm.arsnova.shared.management.RegistryCommands._

class ServiceRegistryActor extends Actor {
  val cluster = Cluster(context.system)

  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("registry", self)

  implicit val ec: ExecutionContext = context.system.dispatcher

  val remoteCommander = context.actorSelection("akka://CommandService@127.0.0.1:8880/user/router")

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {
    // a new member joins the cluster
    case MemberUp(member) => {
      println(s"a member in the cluster is up on ${member.address} with roles ${member.roles}")
      ARSnovaCluster.addMember(member)
    }
    // a member leaves the cluster
    case MemberLeft(member) => {
      println(s"a member left the cluster on ${member.address} with roles ${member.roles}")
      ARSnovaCluster.removeMember(member)
    }

    case SubscribeAck(Subscribe("registry", None, `self`)) =>
      println("registry successfully subscribed to \"registry\"")

    // a service registers an actor for handling service commands
    case RegisterService(serviceType, remote) => {
      println("works")
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
    case s: Any => println(s)
  }
}
