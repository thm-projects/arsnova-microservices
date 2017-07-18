package de.thm.arsnova.managementservice

import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import akka.actor.{Actor, ActorRef, ActorLogging, RootActorPath}
import de.thm.arsnova.shared.management.RegistryCommands._

class ServiceRegistryActor extends Actor with ActorLogging {
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
      log.info(s"a member in the cluster is up on ${member.address} with roles ${member.roles}")
      // manager path is convention!
      val managerRef = context.actorSelection(RootActorPath(member.address) / "user" / "manager")
      managerRef ! RequestRegistration
      ARSnovaCluster.addMember(member)
    }
    // a member leaves the cluster
    case MemberLeft(member) => {
      log.info(s"a member left the cluster on ${member.address} with roles ${member.roles}")
      ARSnovaCluster.removeMember(member)
    }
    // a member is unreachable
    case UnreachableMember(member: Member) => {
      ARSnovaCluster.getServiceActorsForMember(member).map {node =>
        // spread the dead service Actors
      }
    }

    case SubscribeAck(Subscribe("registry", None, `self`)) =>
      log.info("registry successfully subscribed to \"registry\"")

    // a service registers an actor for handling service commands
    case RegisterService(serviceType, remote) => {
      log.info(s"a new service with type $serviceType has registered")
      ARSnovaCluster.addServiceActor(sender.path.address, serviceType, remote)
    }
    case UnregisterService(serviceType, remote) => {
      log.info(s"a service with type $serviceType has unregistered")
      ARSnovaCluster.removeServiceActor(sender.path.address, serviceType, remote)
    }
    case GetActorRefForService(serviceType) => ((ret: ActorRef) => {
      ARSnovaCluster.findActorForService(serviceType) match {
        case Some(ref) => ret ! ref
        case None => ret ! ""
      }
    }) (sender)
    case s: Any => println(s)
  }
}
