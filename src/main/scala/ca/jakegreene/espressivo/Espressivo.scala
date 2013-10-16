package ca.jakegreene.espressivo

import akka.actor.ActorSystem
import akka.io.IO
import akka.actor.Props
import spray.can.Http
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.media.Media
import scalafx.scene.media.MediaPlayer

object Espressivo extends JFXApp {
  implicit val system = ActorSystem("espressivo-system")
  val songLocations = Stream[String]("/resources/keeper.mp3", "/resources/Sing, Sing, Sing (Newer).mp3")
  val songLibrary = loadMusic(songLocations)
  val musicPlayer = system.actorOf(Props(new JukeBox(songLibrary)), "espressivo-player")
  val service = system.actorOf(Props(new HttpServer(musicPlayer)), "espressivo-server")
  val host = "localhost"
  val port = 8080
  IO(Http) ! Http.Bind(service, interface = host, port = port)
  
  stage = new JFXApp.PrimaryStage {
    title = "Espressivo"
    scene = new Scene(200, 200)
  }
  
  private def loadMusic(songLocations: Stream[String]): Map[SongId, Song] = {
    (for {
      (location, id) <- songLocations.zipWithIndex
      resource = getClass.getResource(location)
      media = new Media(resource.toString)
      mediaPlayer = new MediaPlayer(media)
      songId = SongId(id)
      description = SongDescription(songId, location)
      song = Song(description, mediaPlayer)
    } yield (songId, song)).toMap
  }
}