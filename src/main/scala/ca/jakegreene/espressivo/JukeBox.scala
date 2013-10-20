package ca.jakegreene.espressivo

import akka.actor.Actor
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media
import ca.jakegreene.espressivo.music.Song
import ca.jakegreene.espressivo.music.SongController
import akka.actor.Props

case class SongId(id: Int)
case class SongEntry(id: SongId, song: Song)

object JukeBox {
  sealed trait Request
  case class Play(song: SongId) extends Request
  case object Stop extends Request
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
  val musicPlayer = context.actorOf(Props[MusicPlayer], "espressivo-player")
  def receive = {
    case Play(song) => musicPlayer ! MusicPlayer.Play(songLibrary(song))
    case Stop => musicPlayer ! MusicPlayer.Stop
    case GetMusicLibrary => sender ! MusicLibrary(songLibrary.map(tuple => entry(tuple)))
    case GetSong(id) => sender ! SongEntry(id, songLibrary(id))
  }
}