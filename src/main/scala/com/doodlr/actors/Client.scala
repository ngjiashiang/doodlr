package com.doodlr.actors

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, PostStop}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.cluster.typed._
import akka.{actor => classic}
import akka.discovery.{Discovery, Lookup, ServiceDiscovery}
import akka.discovery.ServiceDiscovery.Resolved
import akka.actor.typed.scaladsl.adapter._
import com.doodlr.protocol.JsonSerializable
import scalafx.collections.ObservableHashSet
import scalafx.application.Platform
import akka.cluster.ClusterEvent.ReachabilityEvent
import akka.cluster.ClusterEvent.ReachableMember
import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.ClusterEvent.MemberEvent
import akka.actor.Address
import com.doodlr.ClientMain
import com.doodlr.models.User

object Client {
  sealed trait Command extends JsonSerializable

  //internal protocla
  case object start extends Command

  case class StartJoin(name: String) extends Command

  final case class SendMessageL(target: ActorRef[Client.Command], content: String) extends Command

  final case object FindTheServer extends Command

  private case class ListingResponse(listing: Receptionist.Listing) extends Command

  private final case class MemberChange(event: MemberEvent) extends Command

  private final case class ReachabilityChange(reachabilityEvent: ReachabilityEvent) extends Command

  val members = new ObservableHashSet[User]()

//  val unreachables = new ObservableHashSet[Address]()
//  unreachables.onChange { (ns, _) =>
//    Platform.runLater {
//      ClientMain.control.updateList(members.toList.filter(y => !unreachables.exists(x => x == y.ref.path.address)))
//    }
//  }
//
//  members.onChange { (ns, _) =>
//    Platform.runLater {
//      ClientMain.control.updateList(ns.toList.filter(y => !unreachables.exists(x => x == y.ref.path.address)))
//    }
//  }

  //chat protocol
  final case class MemberList(list: Iterable[User]) extends Command

  final case class Joined(list: Iterable[User]) extends Command

  final case class Message(msg: String, from: ActorRef[Client.Command]) extends Command

  var defaultBehavior: Option[Behavior[Client.Command]] = None
  var remoteOpt: Option[ActorRef[PeerDiscoveryServerActor.Command]] = None
  var nameOpt: Option[String] = None

  def apply(): Behavior[Client.Command] = {
    Behaviors.setup { context =>
      // (1) a ServiceKey is a unique identifier for this actor
      val reachabilityAdapter = context.messageAdapter(ReachabilityChange)
      Cluster(context.system).subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

      // (2) create an ActorRef that can be thought of as a Receptionist
      // Listing “adapter.” this will be used in the next line of code.
      // the Client.ListingResponse(listing) part of the code tells the
      // Receptionist how to get back in touch with us after we contact
      // it in Step 4 below.
      // also, this line of code is long, so i wrapped it onto two lines  //// next time wrap properly
      val listingAdapter: ActorRef[Receptionist.Listing] = context.messageAdapter { listing =>
        println(s"listingAdapter:listing: ${listing.toString}")
        Client.ListingResponse(listing)
      }
      //(3) send a message to the Receptionist saying that we want
      // to subscribe to events related to ServerHello.ServerKey, which
      // represents the Client actor.

      context.system.receptionist ! Receptionist.Subscribe(PeerDiscoveryServerActor.ServerKey, listingAdapter)
      //context.actorOf(RemoteRouterConfig(RoundRobinPool(5), addresses).props(Props[Client.TestActorClassic]()), "testA")
      defaultBehavior = Some(Behaviors.receiveMessage { message =>
        message match {
          case Client.start =>
            context.system.receptionist ! Receptionist.Find(PeerDiscoveryServerActor.ServerKey, listingAdapter)
            Behaviors.same
          // (4) send a Find message to the Receptionist, saying
          // that we want to find any/all listings related to
          // Mouth.MouthKey, i.e., the Mouth actor.
          ////          case FindTheServer =>
          ////            println(s"Clinet Hello: got a FindTheServer message")
          ////            context.system.receptionist ! Receptionist.Find(PeerDiscoveryServerActor.ServerKey, listingAdapter)
          //            Behaviors.same
          // (5) after Step 4, the Receptionist sends us this
          // ListingResponse message. the `listings` variable is
          // a Set of ActorRef of type ServerHello.Command, which
          // you can interpret as “a set of ServerHello ActorRefs.” for
          // this example i know that there will be at most one
          // ServerHello actor, but in other cases there may be more
          // than one actor in this set.

          //// we invoked receptionist.find previously, and we pass in a listing adapter
          //// basically the listing adapter tells the receptionist to give us the server(s) actor ref
          case ListingResponse(PeerDiscoveryServerActor.ServerKey.Listing(listings)) =>
            val xs: Set[ActorRef[PeerDiscoveryServerActor.Command]] = listings
            for (x <- xs) {
              //// since previously its assumed that there could be multiple server actors
              //// then how come we keep overriding remoteOpt?
              //// since we are only subscribing to PeerDiscoveryServerActor.ServerKey which is "PeerDiscovery"
              //// shouldnt we put all remote Opts in an array in this for loop?
              //// since all the servers are doing the samething, which is why they are using the same service key
              //// then how come we decide to use the last one in the xs array? since its always being override until the last one
              remoteOpt = Some(x)
            }
            Behaviors.same
          case StartJoin(name) =>
            nameOpt = Option(name)
            remoteOpt.foreach(_ ! PeerDiscoveryServerActor.JoinChat(name, context.self))
            Behaviors.same
          case Client.Joined(x) =>
//            Platform.runLater {
//              ClientMain.control.displayStatus("joined")
//            }
            members.clear()
            members ++= x
            messageStarted()
          case _ =>
            Behaviors.unhandled

        }
      })
      defaultBehavior.get
    }
  }

  def messageStarted(): Behavior[Client.Command] = Behaviors.receive[Client.Command] { (context, message) =>
    message match {
      case SendMessageL(target, content) =>
        target ! Message(content, context.self)
        Behaviors.same
      case Message(msg, from) =>
        Platform.runLater {
          ///// problem here , cany compile, cannot access the controller from main like this
          ClientMain.whiteboardChatUiController.updateChatList(msg)
        }
        Behaviors.same
      case MemberList(list: Iterable[User]) =>
        //// instead of flushing the whole list, would it be better if we filter it
        members.clear()
        members ++= list
        Behaviors.same
    }
  }.receiveSignal {
    case (context, PostStop) =>
      for (name <- nameOpt;
           remote <- remoteOpt) {
        remote ! PeerDiscoveryServerActor.Leave(name, context.self)
      }
      defaultBehavior.getOrElse(Behaviors.same)
  }
}
