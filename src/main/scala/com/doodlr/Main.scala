package com.doodlr

import com.doodlr.utils.Routable
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import javafx.{scene => jfxs}
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

object Main extends JFXApp with Routable {

  val rootResource = getClass.getResourceAsStream("view/Layout.fxml")
  // initialize the loader object.
  val loader = new FXMLLoader(null, NoDependencyResolver)
  // Load root layout from fxml file.
  loader.load(rootResource)
  // retrieve the root component BorderPane from the FXML
  val roots = loader.getRoot[javafx.scene.layout.BorderPane]

  val image = getClass.getResource("view/main-background.png").toExternalForm
  println(image)
  roots.setStyle("-fx-background-image: url('" + image + "'); " +
    "-fx-background-position: center center; " +
    "-fx-background-repeat: repeat;");

  // initialize stage
  stage = new PrimaryStage {
    title = "hehe"
    scene = new Scene {
      root = roots
    }
    resizable = false
  }

  setPage("Menu")

  stage.onCloseRequest = handle({
    println("hehe")
  })
}