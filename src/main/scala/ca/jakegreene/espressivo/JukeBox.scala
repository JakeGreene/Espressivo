package ca.jakegreene.espressivo

import akka.actor.Actor
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media

case class Play(song: SongId)

class JukeBox extends Actor {
  
  val songLocations = Stream[String]("/resources/keeper.mp3", "/resources/Sing, Sing, Sing (Newer).mp3")
  val musicLibrary = loadMusic(songLocations)
  
  var currentSong: Option[MediaPlayer] = None
  def receive = {
    case Play(song) => {
      if(currentSong.isDefined) currentSong.get.stop()
      val mediaPlayer = musicLibrary(song)
      currentSong = Some(mediaPlayer)
      mediaPlayer.play()
    }
  }
  
  private def loadMusic(songLocations: Stream[String]): Map[SongId, MediaPlayer] = {
    (for {
      (location, id) <- songLocations.zipWithIndex
      resource = getClass.getResource(location)
      media = new Media(resource.toString)
      mediaPlayer = new MediaPlayer(media)
    } yield (SongId(id), mediaPlayer)).toMap
  }
}