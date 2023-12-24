package com.doodlr.view

import javafx.fxml.FXML
import scalafx.scene.input.MouseEvent
import scalafx.Includes.jfxCanvas2sfx
import scalafx.scene.canvas.Canvas
import scalafxml.core.macros.sfxml
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}
import scalafx.scene.shape.Rectangle
import com.doodlr.Main

@sfxml
class CanvasDoodleController (private val canvas: Canvas) {
  val gc = canvas.getGraphicsContext2D
  def handleDrag(event: MouseEvent): Unit = {
    val x = event.getX
    val y = event.getY
    println(s"Mouse dragged at coordinates: ($x, $y)")
    gc.setFill(Color.Black)
    gc.fillRect(x, y, 5, 5)
  }

  def handleClick(event: MouseEvent): Unit = {
    if (event.clickCount > 1) {
      resetCanvas()
    }
  }
  // Function to reset the entire canvas to white
  private def resetCanvas(): Unit = {
    // Clear the entire canvas
    gc.clearRect(0, 0, canvas.width.value, canvas.height.value)

    // Fill the canvas with white color
    gc.setFill(Color.White)
    gc.fillRect(0, 0, canvas.width.value, canvas.height.value)
  }
}