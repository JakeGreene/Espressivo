package ca.jakegreene.espressivo.music

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala

case class SongId(id: Int)
case class SongEntry(id: SongId, song: Song)

object JukeBox {
  
  sealed trait Request
  case object GetMusicLibrary extends Request
  case class GetSong(id: SongId) extends Request
  
  sealed trait Response
  case class Music(songs: Iterable[SongEntry]) extends Response
  
  def apply(library: MusicLibrary) = new JukeBox(library)
}

class JukeBox(songLibrary: MusicLibrary) extends Actor {
  import JukeBox._
  val musicPlayer = context.actorOf(Props[MusicPlayer], "espressivo-player")
  def receive = {
    case GetMusicLibrary => sender ! Music(songLibrary.entries)
    case GetSong(id) => sender ! SongEntry(id, songLibrary(id))
  }
}