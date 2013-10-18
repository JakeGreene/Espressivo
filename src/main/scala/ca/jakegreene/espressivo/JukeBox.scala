package ca.jakegreene.espressivo

import akka.actor.Actor
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media
import ca.jakegreene.espressivo.music.Song
import ca.jakegreene.espressivo.music.SongController

case class SongId(id: Int)
case class SongEntry(id: SongId, song: Song)

object JukeBox {
  sealed trait Request
  case class Play(song: SongId) extends Request
  case object GetMusicLibrary extends Request
  case class GetSong(id: SongId) extends Request
  sealed trait Response
  case class MusicLibrary(songs: Iterable[SongEntry]) extends Response
  
  def apply(library: Map[SongId, Song]) = new JukeBox(library)
  def entry(id: SongId, song: Song): SongEntry = SongEntry(id, song)
  def entry(tuple: Tuple2[SongId, Song]): SongEntry = (entry _).tupled(tuple)
}

class JukeBox(songLibrary: Map[SongId, Song]) extends Actor {
  import JukeBox._
  var currentSong: Option[SongController] = None
  def receive = {
    case Play(song) => playSong(song)
    case GetMusicLibrary => sender ! MusicLibrary(songLibrary.map(tuple => entry(tuple)))
    case GetSong(id) => sender ! SongEntry(id, songLibrary(id))
  }
  
  private def playSong(id: SongId) {
    if(currentSong.isDefined) currentSong.get.stop()
    val song = songLibrary(id)
    val controller = song.createController()
    currentSong = Some(controller)
    controller.play()
  }
}