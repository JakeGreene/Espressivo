package ca.jakegreene.espressivo

import akka.actor.ActorSystem
import akka.io.IO
import akka.actor.Props
import spray.can.Http
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.media.Media
import scalafx.scene.media.MediaPlayer
import ca.jakegreene.espressivo.music.Song
import ca.jakegreene.espressivo.music.ScalafxSong
import java.io.File
import java.net.URI
import ca.jakegreene.espressivo.music.MusicLibrary
import ca.jakegreene.espressivo.settings.Settings

object Espressivo extends JFXApp {
  implicit val system = ActorSystem("espressivo-system")
  val settings = Settings(system)
  def fxCreator(uri: URI) = {
    val media = new Media(uri.toString)
    new ScalafxSong(media)
  }
  val songLibrary = MusicLibrary.inDirectory(settings.MusicRoot, fxCreator)
  val musicPlayer = system.actorOf(Props(new JukeBox(songLibrary)), "espressivo-player")
  val service = system.actorOf(Props(new HttpServer(musicPlayer)), "espressivo-server")
  val host = "localhost"
  val port = 8080
  IO(Http) ! Http.Bind(service, interface = host, port = port)
  
  stage = new JFXApp.PrimaryStage {
    title = "Espressivo"
    scene = new Scene(200, 200)
  }
}