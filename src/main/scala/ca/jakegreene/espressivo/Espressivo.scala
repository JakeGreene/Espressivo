package ca.jakegreene.espressivo

import akka.actor.ActorSystem
import akka.io.IO
import akka.actor.Props
import spray.can.Http
import scalafx.application.JFXApp
import scalafx.scene.Scene

object Espressivo extends JFXApp {
  implicit val system = ActorSystem("espressivo-system")
  val musicPlayer = system.actorOf(Props[JukeBox], "espressivo-player")
  val service = system.actorOf(Props(new HttpServer(musicPlayer)), "espressivo-server")
  val host = "localhost"
  val port = 8080
  IO(Http) ! Http.Bind(service, interface = host, port = port)
  
  stage = new JFXApp.PrimaryStage {
    title = "Espressivo"
    scene = new Scene(200, 200)
  }
}