package com.doodlr.utils
import scalafxml.core.{NoDependencyResolver, FXMLView, FXMLLoader}
import com.doodlr.Main
import javafx.{scene => jfxs}
trait Routable {
  def setPage(pageName : String): Unit = {
    val pagePath = "view/"+pageName+".fxml"
    val resource = getClass.getResourceAsStream(pagePath)
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(resource);
    val roots = loader.getRoot[jfxs.layout.AnchorPane]
    Main.roots.setCenter(roots)
  }
}
