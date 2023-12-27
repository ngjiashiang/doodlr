package com.doodlr

import com.typesafe.config.{Config, ConfigFactory}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.cluster.typed._
import com.doodlr.actors.PeerDiscoveryServerActor

object ServerMain extends App {
  val config: Config = ConfigFactory.load("configs/server-config.conf")
  val mainSystem = akka.actor.ActorSystem("DoodlrServerHotel", config) //classic
  val typedSystem: ActorSystem[Nothing] = mainSystem.toTyped
  val cluster = Cluster(typedSystem)
  cluster.manager ! Join(cluster.selfMember.address)
  AkkaManagement(mainSystem).start()
  //val serviceDiscovery = Discovery(mainSystem).discovery
  ClusterBootstrap(mainSystem).start()
  //val greeterMain: ActorSystem[Server.Command] = ActorSystem(Server(), "HelloSystem")
  mainSystem.spawn(PeerDiscoveryServerActor(), "PeerDiscoveryServer")
}
