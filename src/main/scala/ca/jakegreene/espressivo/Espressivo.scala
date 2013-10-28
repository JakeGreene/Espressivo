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
import ca.jakegreene.espressivo.music.ScalafxSong
import ca.jakegreene.espressivo.music.JukeBox
import ca.jakegreene.espressivo.settings.Settings
import com.typesafe.config.ConfigFactory

object Espressivo extends JFXApp {
  implicit val system = ActorSystem("espressivo-system")
  val userConfig = ConfigFactory.parseFile(new File(parameters.raw(0)))
  val settings = Settings(system, userConfig)
  private def fxCreator(uri: URI) = {
    val media = new Media(uri.toString)
    new ScalafxSong(media)
  }
  val songLibrary = MusicLibrary.inDirectory(settings.musicRoot, fxCreator)
  val musicPlayer = system.actorOf(Props(new JukeBox(songLibrary)), "espressivo-player")
  val service = system.actorOf(Props(new HttpServer(musicPlayer)), "espressivo-server")
  IO(Http) ! Http.Bind(service, interface = settings.host, port = settings.port)
  
  stage = new JFXApp.PrimaryStage {
    title = "Espressivo"
    scene = new Scene(200, 200)
  }
}