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

object Espressivo extends JFXApp {
  implicit val system = ActorSystem("espressivo-system")
  val songLocations = Stream[String]("/resources/keeper.mp3", "/resources/Sing, Sing, Sing (Newer).mp3")
  val songLibrary = loadMusic(new File("/home/jake/Music"))
  val musicPlayer = system.actorOf(Props(new JukeBox(songLibrary)), "espressivo-player")
  val service = system.actorOf(Props(new HttpServer(musicPlayer)), "espressivo-server")
  val host = "localhost"
  val port = 8080
  IO(Http) ! Http.Bind(service, interface = host, port = port)
  
  stage = new JFXApp.PrimaryStage {
    title = "Espressivo"
    scene = new Scene(200, 200)
  }
  
  private def loadMusic(root: File): Map[SongId, Song] = {
    val locations = findAllMusicLocations(root)
    return loadMusic(locations)
  }
  
  private def findAllMusicLocations(root: File): Seq[URI] = {
    val files = root.listFiles().filter(_.isFile())
    val directories = root.listFiles().filter(_.isDirectory())
    return filesToURI(files) ++ directories.flatMap(findAllMusicLocations(_))
  }
  
  private def filesToURI(files: Seq[File]): Seq[URI] = {
    files.map(_.toURI())
  }
  
  private def loadMusic(songLocations: Seq[URI]): Map[SongId, Song] = {
    songLocations.foreach(println)
    (for {
      (location, id) <- songLocations.zipWithIndex
      if supported(location)
      media = new Media(location.toString)
      song = new ScalafxSong(media)
      songId = SongId(id)
    } yield (songId, song)).toMap
  }
  
  private def supported(song: URI): Boolean = {
    val suffixStart = song.getRawPath().lastIndexOf(".")
    val suffix = song.getRawPath().substring(suffixStart+1)
    Seq("mp3", "wav", "aif", "aiff") contains suffix
  }
}