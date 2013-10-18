package ca.jakegreene.espressivo

import akka.actor.Actor
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media
import ca.jakegreene.espressivo.music.Song
import ca.jakegreene.espressivo.music.SongController

sealed trait Request
case class Play(song: SongId) extends Request
case object GetMusicLibrary extends Request
case class GetSong(id: SongId) extends Request
sealed trait Response
case class MusicLibrary(songs: Iterable[SongDescription]) extends Response

case class SongId(id: Int)
case class SongDescription(id: SongId, name: String)

class JukeBox(songLibrary: Map[SongId, Song]) extends Actor {
  var currentSong: Option[SongController] = None
  def receive = {
    case Play(song) => playSong(song)
    case GetMusicLibrary => sender ! MusicLibrary(songLibrary.map(entry => SongDescription(entry._1, entry._2.title)))
    case GetSong(id) => sender ! SongDescription(id, songLibrary(id).title)
  }
  
  private def playSong(id: SongId) {
    if(currentSong.isDefined) currentSong.get.stop()
    val song = songLibrary(id)
    val controller = song.createController()
    currentSong = Some(controller)
    controller.play()
  }
}