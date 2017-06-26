package de.thm.arsnova.shared.actors

import de.thm.arsnova.shared.management.RegistryCommands._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.cluster.{Cluster, Member}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, SubscribeAck}

class ServiceManagementActor(serviceType: String, serviceActorRef: ActorRef) extends Actor {
  //val cluster = Cluster(context.system)

  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("registry", self)

  override def preStart(): Unit = {
    mediator ! Publish("registry", RegisterService(serviceType, serviceActorRef))
  }

  override def postStop(): Unit = {
    mediator ! Publish("registry", UnregisterService(serviceType, serviceActorRef))
  }

  def receive = {
    case SubscribeAck(Subscribe("registry", None, `self`)) =>
      println("manager successfully subscribed to \"registry\"")

    case "startup" => {
      println("startup")
      mediator ! Publish("registry", RegisterService(serviceType, serviceActorRef))
    }
    case RegisterService(a, b) => println("whyyyyyyyyy only here")
  }
}
