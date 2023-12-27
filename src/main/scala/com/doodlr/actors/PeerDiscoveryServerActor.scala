package com.doodlr.actors

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import com.doodlr.models.User
import com.doodlr.protocol.JsonSerializable
import scalafx.collections.ObservableHashSet

object PeerDiscoveryServerActor {
  sealed trait Command extends JsonSerializable
  case class JoinChat(name: String, from: ActorRef[Client.Command]) extends Command
  case class Leave(name: String, from: ActorRef[Client.Command]) extends Command
  val ServerKey: ServiceKey[PeerDiscoveryServerActor.Command] = ServiceKey("PeerDiscovery")
  val members = new ObservableHashSet[User]()


  members.onChange{(ns, _) =>
    for(member <- ns){
      member.ref ! Client.MemberList(members.toList)
    }
  }

  def apply(): Behavior[PeerDiscoveryServerActor.Command] = Behaviors.setup { context =>

    context.system.receptionist ! Receptionist.Register(ServerKey, context.self)

    Behaviors.receiveMessage { message =>
      message match {
        case JoinChat(name, from) =>
          members += User(name, from)
          from ! Client.Joined(members.toList)
          Behaviors.same
        case Leave(name, from) =>
          members -= User(name, from)
          Behaviors.same
      }
    }
  }
}
