package de.thm.arsnova.managementservice

import akka.actor.{ActorRef, Address}
import akka.cluster.Member

object ARSnovaCluster {
  private var nodes = List[Member]()
  // (akka address, serviceType) -> serviceActor
  private var serviceActors = collection.mutable.Map[(Address, String), ActorRef]()

  def addMember(member: Member): Unit = {
    // add member to cluster nodes. member hasn't published what microservice-type it is
    // could be done with member.roles, currently not sure if useful since microservices have to announce actors anyway
    nodes = nodes :+ member
  }

  def removeMember(member: Member): Unit = {
    nodes = nodes.filterNot(_ == member)
  }

  def addServiceActor(address: Address, role: String, ref: ActorRef): Unit = {
    // lookup the node
    serviceActors += (address, role) -> ref
  }

  def removeServiceActor(address: Address, role: String): Option[ActorRef] = {
    serviceActors.remove((address, role))
  }
}