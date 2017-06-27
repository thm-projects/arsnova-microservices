package de.thm.arsnova.managementservice

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import akka.actor.{Actor, RootActorPath}
import de.thm.arsnova.shared.management.RegistryCommands._

class ServiceRegistryActor extends Actor {
  val cluster = Cluster(context.system)

  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("registry", self)

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
      // manager path is convention!
      val managerRef = context.actorSelection(RootActorPath(member.address) / "user" / "manager")
      managerRef ! RequestRegistration
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
      println(s"a new service with type $serviceType has registered")
      ARSnovaCluster.addServiceActor(sender.path.address, serviceType, remote)
    }
    case UnregisterService(serviceType, remote) => {
      println(s"a service with type $serviceType has unregistered")
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
