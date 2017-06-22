package de.thm.arsnova.managementservice

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.{Actor, ActorRef, RootActorPath, Address}
import akka.pattern.pipe
import de.thm.arsnova.shared.management.RegistryCommands._

class ServiceRegistryActor extends Actor {
  val cluster = Cluster(context.system)

  implicit val ec: ExecutionContext = context.system.dispatcher

  val remoteCommander = context.actorSelection("akka://CommandService@127.0.0.1:8880/user/router")

  // this is needed to remove refs from commandservice cache since actorref can't be resolved when node is exiting
  val nodes = collection.mutable.Map[(String, Address), ActorRef]()

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {
    // a new member joins the cluster
    case MemberUp(newMember) => {
      newMember.roles.foreach { role =>
        // actorSelection needs to be resolved (async!)
        val actorRefFuture = context.actorSelection(RootActorPath(newMember.address) / "user" / "dispatcher").resolveOne(5.seconds)
        actorRefFuture.onComplete {
          case Success(ref) => {
            nodes.get((role, newMember.address)) match {
              // only needed when service has many roles. prevents having multiple entries for the same microservice
              case Some(e) =>
              case None => {
                println(s"new service with role $role registered on address ${newMember.address}")
                nodes += (role, newMember.address) -> ref
              }
            }
            remoteCommander ! RegisterService(role, ref)
          }
          case Failure(t) => {
            println(s"Couldn't resolve an ActorRef for service with role $role, error message: $t")
          }
        }
      }
    }
    case MemberLeft(member) => {
      member.roles.foreach { role =>
        nodes.remove((role, member.address)) match {
          case Some(ref) => remoteCommander ! UnregisterService(role, ref)
          case None => println("a member w/o an entry in nodes (Map) left the cluster")
        }
      }
    }

    case RegisterService(serviceType, remote) =>
      println(s"$serviceType has registered")
      //services(serviceType) = remote
      remoteCommander ! RegisterService(serviceType, remote)
  }
}
