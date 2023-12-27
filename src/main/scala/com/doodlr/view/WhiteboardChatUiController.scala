package com.doodlr.view

import akka.actor.typed.ActorRef
import javafx.fxml.FXML
import scalafx.scene.input.MouseEvent
import scalafx.Includes.jfxCanvas2sfx
import scalafx.scene.canvas.Canvas
import scalafxml.core.macros.sfxml
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Paint, Stop}
import scalafx.scene.shape.Rectangle
import scalafx.scene.control.{CheckBox, ColorPicker, Label, ListView, Slider, TextArea}
import com.doodlr.ClientMain
import com.doodlr.actors.Client
import com.doodlr.models.User
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent

import scala.collection.mutable.ArrayBuffer

@sfxml
class WhiteboardChatUiController(private val canvas: Canvas, private val dottedCheckbox: CheckBox,
                                 private val colourPicker: ColorPicker, private val slider: Slider,
                                 private val lineThickness: Label, private val chatInput: TextArea,
                                 private val chatList: ListView[String]) {

  val gc = canvas.getGraphicsContext2D

  // set initial line colour and thickness
  gc.setStroke(Color.Black)
  gc.setLineWidth(1)
  // change the canvas background to white
  gc.setFill(Color.White)
  gc.fillRect(0, 0, canvas.width.value, canvas.height.value)
  // set the colour picker value to black first
  colourPicker.setValue(Color.Black)

  var localDrawDotted: Boolean = false
  var localColorCode: String = colourPicker.getValue.toString
  var localLineWidth: Double = 1
  var lastXCoordinate: Double = 0
  var lastYCoordinate: Double = 0

  val recievedChats: ObservableBuffer[String] = new ObservableBuffer[String]()
  chatList.items = recievedChats

  slider.valueProperty().addListener(_ => {
    val value: Int = slider.getValue.toInt
    lineThickness.setText(s"${value}")
    localLineWidth = value
  })

  // Quick change line colour
  def changeBlack(event: MouseEvent): Unit = {
    colourPicker.setValue(Color.Black)
    localColorCode = colourPicker.getValue.toString
  }

  def changeBlue(event: MouseEvent): Unit = {
    colourPicker.setValue(Color.Blue)
    localColorCode = colourPicker.getValue.toString
  }

  def changeRed(event: MouseEvent): Unit = {
    colourPicker.setValue(Color.Red)
    localColorCode = colourPicker.getValue.toString
  }

  def handleErazer(event: MouseEvent): Unit = {
    colourPicker.setValue(Color.White)
    localColorCode = colourPicker.getValue.toString
  }

  def updateCanvas(colorCode: String, isDotted: Boolean, previousXCoordinate: Double, previousYCoordinate: Double, currentXCoordinate: Double, currentYCoordinate: Double, lineWidth: Double): Unit = {
    gc.setLineWidth(lineWidth)
    gc.setStroke(Paint.valueOf(colorCode))
    print("drawing start | ")
    print(s"line width: ${lineWidth} | ")
    print(s"color: ${colorCode} | ")
    if (isDotted) {
      println("dotted")
      gc.setLineDashes(8.0, 25.0) // Set 8-pixel dash, 25-pixel space
      println(s"x: ${currentXCoordinate}, y: ${currentYCoordinate}")
      gc.strokeLine(previousXCoordinate, previousYCoordinate, currentXCoordinate, currentYCoordinate)
      gc.setLineDashes() // Reset to solid line
    } else {
      println("non dotted")
      gc.strokeLine(previousXCoordinate, previousYCoordinate, currentXCoordinate, currentYCoordinate)
      println(s"x: ${currentXCoordinate}, y: ${currentYCoordinate}")
    }

  }

  def changeColour(action: ActionEvent): Unit = {
    localColorCode = colourPicker.getValue.toString
    //    test if really can be passed by another akka actor
    //    in handle drag, the event.get coordinates isnt always continous, i.e x=1 when u drag fast, the next time that function is called, x might be 10 or smth
    //    depends on how fast u drag
    //    strokelines in updateCanvas draws a line and joins from x = 1 to x = 10
    //    so if the distance that is not continous is more that 8, we will have a space because of linedashes
    //    coz strokeline creates a line to join both x
    //    updateCanvas(localColorCode, isDotted = true, 9, 10, 10, 10, 20)
    //    updateCanvas(localColorCode, isDotted = true, 10, 10, 10, 10, 20)
    //    updateCanvas(localColorCode, isDotted = true, 11, 10, 10, 10, 20)
    //    updateCanvas(localColorCode, isDotted = true, 12, 10, 10, 10, 20)
    //    updateCanvas(localColorCode, isDotted = true, 13, 10, 10, 10, 20)
    //    updateCanvas(localColorCode, isDotted = true, 20, 10, 10, 10, 20)
    //    updateCanvas(localColorCode, isDotted = true, 25, 10, 10, 10, 20)
    //    updateCanvas(localColorCode, isDotted = true, 30, 10, 10, 10, 20)
  }

  def handleDotted(action: ActionEvent): Unit = {
    if (dottedCheckbox.isSelected) {
      localDrawDotted = true
      println("Dotted line triggered.")
    } else {
      localDrawDotted = false
    }
  }

  def handleMousePressed(event: MouseEvent): Unit = {
    lastXCoordinate = event.getX
    lastYCoordinate = event.getY
  }

  def handleDrag(event: MouseEvent): Unit = {
    val currentX = event.getX
    val currentY = event.getY
    Client.members.foreach { user =>
      ClientMain.userRef ! Client.SendDrawingL(user.ref, localColorCode, localDrawDotted, lastXCoordinate, lastYCoordinate, currentX, currentY, localLineWidth)
    }
    lastXCoordinate = event.getX
    lastYCoordinate = event.getY
  }

  def handleClick(event: MouseEvent): Unit = {
    if (event.clickCount > 1) {
      Client.members.foreach { user =>
        ClientMain.userRef ! Client.ClearDrawingL(user.ref)
      }
      println("Canvas cleared.")
    }
  }


  // Function to reset the entire canvas to white
  def resetCanvas(): Unit = {
    // Clear the entire canvas
    gc.clearRect(0, 0, canvas.width.value, canvas.height.value)

    // Fill the canvas with white color
    gc.setFill(Color.White)
    gc.fillRect(0, 0, canvas.width.value, canvas.height.value)
  }

  def handleSendChat(): Unit = {
    val textToSend = ClientMain.userName + ": " + chatInput.getText
    Client.members.foreach { user =>
      ClientMain.userRef ! Client.SendMessageL(user.ref, textToSend)
    }
    chatInput.setText("")
  }

  def updateChatList(chatContext: String): Unit = {
    chatList.getItems.add(chatContext)
  }
}