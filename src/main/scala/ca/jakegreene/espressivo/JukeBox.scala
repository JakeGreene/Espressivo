package ca.jakegreene.espressivo

import akka.actor.Actor
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media

sealed trait Request
case class Play(song: SongId) extends Request
case object GetMusicLibrary extends Request
sealed trait Response
case class MusicLibrary(library: Iterable[SongDescription]) extends Response


case class Song(description: SongDescription, media: MediaPlayer)

class JukeBox extends Actor {
  
  val songLocations = Stream[String]("/resources/keeper.mp3", "/resources/Sing, Sing, Sing (Newer).mp3")
  val songLibrary = loadMusic(songLocations)
  
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
  
  var currentSong: Option[MediaPlayer] = None
  def receive = {
    case Play(song) => playSong(song)
    case GetMusicLibrary => sender ! MusicLibrary(songLibrary.values.map(song => song.description))
  }
  
  private def playSong(id: SongId) {
    if(currentSong.isDefined) currentSong.get.stop()
    val mediaPlayer = songLibrary(id).media
    currentSong = Some(mediaPlayer)
    mediaPlayer.play()
  }
}