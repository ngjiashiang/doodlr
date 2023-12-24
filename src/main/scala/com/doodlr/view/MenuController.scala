package com.doodlr.view

import javafx.fxml.FXML
import scalafx.scene.input.MouseEvent
import scalafx.Includes.jfxCanvas2sfx
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.{Alert, Button, TextField}
import scalafxml.core.macros.sfxml
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}
import scalafx.scene.shape.Rectangle
import com.doodlr.Main
import scalafx.event.ActionEvent
import scalafx.scene.control.Alert.AlertType

@sfxml
class MenuController (private val joinButton: Button, private val nameField: TextField) {
  def handleJoin(): Unit = {
    if (nameField.getText.nonEmpty) {
      Main.setPage("WhiteboardChatUi")
    } else {
      new Alert(AlertType.Error) {
        title = "Error"
        headerText = "You need to provide a name to join."
      }.showAndWait()
    }
  }
}