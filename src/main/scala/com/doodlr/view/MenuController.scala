package com.doodlr.view

import akka.actor.typed.ActorRef
import javafx.fxml.FXML
import scalafx.scene.input.MouseEvent
import scalafx.Includes.jfxCanvas2sfx
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.{Alert, Button, TextField}
import scalafxml.core.macros.sfxml
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}
import scalafx.scene.shape.Rectangle
import com.doodlr.ClientMain
import com.doodlr.actors.Client
import scalafx.event.ActionEvent
import scalafx.scene.control.Alert.AlertType
import scalafx.Includes._

@sfxml
class MenuController (private val joinButton: Button, private val nameField: TextField) {
  var clientActorRef: Option[ActorRef[Client.Command]] = None

  def handleJoin(): Unit = {
    val userName = nameField.getText
    if (userName.nonEmpty) {
      ClientMain.userName = userName
      clientActorRef map (_ ! Client.StartJoin(userName))
      ClientMain.setPageWhiteBoard()
    } else {
      new Alert(AlertType.Error) {
        title = "Error"
        headerText = "You need to provide a name to join."
      }.showAndWait()
    }
  }
}