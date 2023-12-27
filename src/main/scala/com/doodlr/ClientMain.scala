package com.doodlr

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{FXMLLoader, NoDependencyResolver}

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.cluster.typed._
import akka.{actor => classic}
import akka.discovery.{Discovery, Lookup, ServiceDiscovery}
import akka.discovery.ServiceDiscovery.Resolved
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.adapter._
import com.doodlr.actors.Client
import com.typesafe.config.{Config, ConfigFactory}
import javafx.{scene => jfxs}

object ClientMain extends JFXApp {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  val clientConfig: Config = ConfigFactory.load("configs/client-config.conf")
  val mainSystem = akka.actor.ActorSystem("DoodlrSystem", clientConfig)
  val greeterMain: ActorSystem[Nothing] = mainSystem.toTyped
  val cluster = Cluster(greeterMain)
  //  val discovery: ServiceDiscovery = Discovery(mainSystem).discovery

  val userRef = mainSystem.spawn(Client(), "DoodlrClient")

  userRef ! Client.start

  //  def joinPublicSeedNode(): Unit = {
  //    val lookup: Future[Resolved] =
  //      discovery.lookup(Lookup("wm.hep88.com").withPortName("hellosystem").withProtocol("tcp"), 1.second)
  //
  //    lookup.foreach(x => {
  //      val result = x.addresses
  //      result map { x =>
  //        val address = akka.actor.Address("akka", "HelloSystem", x.host, x.port.get)
  //        cluster.manager ! JoinSeedNodes(List(address))
  //      }
  //    })
  //  }

  def joinLocalSeedNode(): Unit = {
//    val serverConfig: Config = ConfigFactory.load("configs/server-config.conf")
//    val serverIpAddress: String = serverConfig.getString("akka.actor.remote.artery.canonical.hostname")
//    val serverPort: Int = serverConfig.getInt("akka.actor.remote.artery.canonical.port")
    // special Akka Actor Name Service (AANS)
    val address = akka.actor.Address("akka",
      "DoodlrSystem",
      "192.168.100.5",
      2222
    )
    cluster.manager ! JoinSeedNodes(List(address))
  }

  joinLocalSeedNode()

  val rootResource = getClass.getResource("view/Layout.fxml")
  // initialize the loader object.
  val loader = new FXMLLoader(rootResource, NoDependencyResolver)
  // Load root layout from fxml file.
  loader.load()
  // retrieve the root component BorderPane from the FXML
  val roots = loader.getRoot[javafx.scene.layout.BorderPane]

  val image = getClass.getResource("view/main-background.png").toExternalForm

  roots.setStyle("-fx-background-image: url('" + image + "'); " +
    "-fx-background-position: center center; " +
    "-fx-background-repeat: repeat;");

//  val control = loader.getController[com.doodlr.view.WhiteboardChatUiController#Controller]()
//  control.chatClientRef = Option(userRef)

  // initialize stage
  stage = new PrimaryStage {
    title = "hehe"
    scene = new Scene {
      root = roots
    }
    resizable = false
  }

  object Menu {
    val resource = getClass.getResource("view/Menu.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()
    val roots = loader.getRoot[jfxs.layout.AnchorPane]
    def load(): Unit = {
      ClientMain.roots.setCenter(roots)
    }
  }

  object WhiteboardChatUi {
    val resource = getClass.getResource("view/WhiteboardChatUi.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()
    val roots = loader.getRoot[jfxs.layout.AnchorPane]
    val control = loader.getController[com.doodlr.view.WhiteboardChatUiController#Controller]()
    def load(): Unit = {
      ClientMain.roots.setCenter(roots)
    }
  }

//  def setPageWhiteBoard(): Unit = {
//    val resource = getClass.getResource(s"view/WhitboardChatUi.fxml")
//    val loader = new FXMLLoader(resource, NoDependencyResolver)
//    loader.load()
//    val roots = loader.getRoot[jfxs.layout.AnchorPane]
//    this.roots.setCenter(roots)
//    val whiteboardChatUiController = loader.getController[com.doodlr.view.WhiteboardChatUiController#Controller]()
//  }

  var userName = ""
  Menu.load()

  stage.onCloseRequest = handle({
    println("hehe")
  })
}
