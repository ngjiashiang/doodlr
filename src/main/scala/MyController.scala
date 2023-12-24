import javafx.event.ActionEvent
import scalafx.scene.control.Button
import scalafx.scene.layout.StackPane
import scalafxml.core.macros.sfxml

@sfxml
class MyController(private val button: Button) {

  def handleButtonClick(actionEvent: ActionEvent): Unit = {
    println("Button clicked!")
  }
}