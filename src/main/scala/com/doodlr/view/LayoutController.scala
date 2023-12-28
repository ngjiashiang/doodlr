package com.doodlr.view

import com.doodlr.ClientMain
import scalafxml.core.macros.sfxml


@sfxml
class LayoutController () {
  //function to close the program
  def handleClose(): Unit = {
    System.exit(0)
  }

  //function to access to quick guide page
  def showGuide(): Unit = {
    ClientMain.displayGuide()
  }
}