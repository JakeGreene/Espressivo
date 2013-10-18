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

object JukeBox {
  def apply(library: Map[SongId, Song]) = new JukeBox(library)
  def describe(id: SongId, song: Song): SongDescription = SongDescription(id, song.title)
  def describe(tuple: Tuple2[SongId, Song]): SongDescription = (describe _).tupled(tuple)
}

class JukeBox(songLibrary: Map[SongId, Song]) extends Actor {
  import JukeBox._
  var currentSong: Option[SongController] = None
  def receive = {
    case Play(song) => playSong(song)
    case GetMusicLibrary => sender ! MusicLibrary(songLibrary.map(entry => describe(entry)))
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