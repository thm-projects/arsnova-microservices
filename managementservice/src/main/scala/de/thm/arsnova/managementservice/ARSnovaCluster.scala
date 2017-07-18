package de.thm.arsnova.managementservice

import akka.actor.{ActorRef, Address}
import akka.cluster.Member

object ARSnovaCluster {
  private var nodes = List[Member]()
  private var services = List[ARSnovaClusterNode]()

  def addMember(member: Member): Unit = {
    // add member to cluster nodes. member hasn't published what microservice-type it is
    // could be done with member.roles, currently not sure if useful since microservices have to announce actors anyway
    nodes = nodes :+ member
  }

  def removeMember(member: Member): Unit = {
    nodes = nodes.filterNot(_ == member)
  }

  def getServiceActorsForMember(member: Member): Seq[(String, ActorRef)] = {
    services.find(_.address == member.address) match {
      case Some(node) => node.serviceActors
      case None => Nil
    }
  }

  def addServiceActor(address: Address, role: String, ref: ActorRef): Unit = {
    // lookup the node
    services.find(_.address == address) match {
      // service has already registered at least one service actor
      case Some(node) => {
        services = services.filterNot(_.address == address) :+ ARSnovaClusterNode(address, node.serviceActors :+ (role, ref))
      }
      // first service actor from this node
      case None => {
        services = services :+ ARSnovaClusterNode(address, Seq((role, ref)))
      }

    }
  }

  def findActorForService(serviceType: String): Option[ActorRef] = {
    val allActors: Seq[(String, ActorRef)] = services.flatMap(_.serviceActors)
    allActors.find(tuple => tuple._1 == serviceType).map(_._2)
  }

  def removeServiceActor(address: Address, role: String, ref: ActorRef): Unit = {
    services.find(_.address == address) match {
      // there is an actual node with this address
      case Some(node) => {
        node.serviceActors.filterNot(_ == (role, ref)) match {
          // this node has more service actors registered
          case s: Seq[(String, ActorRef)] =>
            services = services.filterNot(_.address == address) :+ ARSnovaClusterNode(address, s)
          // node has no more registered service actors -> remove
          case Nil =>
            services = services.filterNot(_.address == address)
        }
      }
      case None => {
        println("trying to remove a service actor that's not registered")
      }
    }
  }
}

case class ARSnovaClusterNode(address: Address, serviceActors: Seq[(String, ActorRef)])
