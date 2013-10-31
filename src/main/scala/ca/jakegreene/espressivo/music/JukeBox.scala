package ca.jakegreene.espressivo.music

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala

case class SongId(id: Int)
case class SongEntry(id: SongId, song: Song)

object JukeBox {
  case class PlaylistId(id: Int)
  case class Playlist(name: String, songs: List[SongId])
  
  sealed trait Request
  case class Play(song: SongId) extends Request
  case object Stop extends Request
  case object Pause extends Request
  case class Start(id: PlaylistId) extends Request
  case object GetMusicLibrary extends Request
  case class GetSong(id: SongId) extends Request
  case class StorePlaylist(playlist: Playlist) extends Request
  case class GetPlaylist(id: PlaylistId) extends Request
  case object GetPlaylists extends Request
  
  sealed trait Response
  case class Music(songs: Iterable[SongEntry]) extends Response
  case class Playlists(all: Iterable[Playlist]) extends Response
  
  def apply(library: MusicLibrary) = new JukeBox(library)
}

class JukeBox(songLibrary: MusicLibrary) extends Actor {
  import JukeBox._
  val musicPlayer = context.actorOf(Props[MusicPlayer], "espressivo-player")
  val playlists = scala.collection.mutable.Map[PlaylistId, Playlist]()
  def receive = {
    case Play(song) => musicPlayer ! MusicPlayer.Play(songLibrary(song))
    case Stop => musicPlayer ! MusicPlayer.Stop
    case Pause => musicPlayer ! MusicPlayer.Pause
    case GetMusicLibrary => sender ! Music(songLibrary.entries)
    case GetSong(id) => sender ! SongEntry(id, songLibrary(id))
    case StorePlaylist(playlist) => {
      val id = PlaylistId(playlists.size + 1)
      playlists += (id -> playlist)
      sender ! Playlists(playlists.values)
    }
    case GetPlaylist(id) => sender ! playlists(id)
    case GetPlaylists => sender ! Playlists(playlists.values)
    case Start(id) => 
  }
}