package ca.jakegreene.espressivo

import akka.actor.Actor
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media

sealed trait Request
case class Play(song: SongId) extends Request
case object GetMusicLibrary extends Request
case class GetSong(id: SongId) extends Request
sealed trait Response
case class MusicLibrary(songs: Iterable[SongDescription]) extends Response


case class Song(description: SongDescription, media: MediaPlayer)

class JukeBox(songLibrary: Map[SongId, Song]) extends Actor {
  var currentSong: Option[MediaPlayer] = None
  def receive = {
    case Play(song) => playSong(song)
    case GetMusicLibrary => sender ! MusicLibrary(songLibrary.values.map(song => song.description))
    case GetSong(id) => sender ! songLibrary(id).description
  }
  
  private def playSong(id: SongId) {
    if(currentSong.isDefined) currentSong.get.stop()
    val mediaPlayer = songLibrary(id).media
    currentSong = Some(mediaPlayer)
    mediaPlayer.play()
  }
}