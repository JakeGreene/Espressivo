package ca.jakegreene.espressivo.music

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.ListBuffer
import akka.actor.ActorLogging
import akka.actor.FSM
import akka.actor.LoggingFSM
import scala.collection.immutable.Queue

object MusicStream {
  sealed trait Request
  case object Activate extends Request
  case object Suspend extends Request
  case class Append(song: SongEntry) extends Request
  case object GetStatus extends Request
  
  sealed trait State
  case object Ready extends State
  case object Active extends State
  case object Suspended extends State
  case object Waiting extends State
  
  sealed trait Data
  case class Status(nextSongs: Queue[SongEntry], current: Option[SongEntry]) extends Data
  
  case class StreamStatus(nextSongs: Queue[SongEntry], current: Option[SongEntry], state: State)
}
import MusicStream.{State, Data}
class MusicStream(musicPlayer: ActorRef) extends Actor with ActorLogging with LoggingFSM[State, Data] {
  import MusicStream._
  
  musicPlayer ! MusicPlayer.ListenForSongEnd(self)
  startWith(Ready, Status(Queue(), None))
  
  when(Ready) {
    case Event(Activate, Status(Queue(), None)) => goto (Waiting) using Status(Queue(), None)
    case Event(Activate, Status(songs: Queue[SongEntry], None)) => {
      setupSong(songs)
    }
    case Event(Append(song), Status(songs, None)) => stay using Status(songs enqueue song, None) // Newest song at the head of the list
    case Event(Suspend, _) => stay
    case Event(MusicPlayer.SongFinished(_), _) => stay
  }
  
  when(Active) {
    case Event(Append(song), Status(songs, playing)) => stay using Status(songs enqueue song, playing)
    case Event(Activate, _) => stay
    case Event(Suspend, _) => goto(Suspended)
    case Event(MusicPlayer.SongFinished(song), Status(Queue(), Some(current))) if song equals current.song => goto(Waiting) using Status(Queue(), None)
    case Event(MusicPlayer.SongFinished(song), Status(songs: Queue[SongEntry], Some(current))) if song equals current.song => {
      setupSong(songs)
    }
  }
  
  when(Waiting) {
    case Event(Append(songEntry), Status(Queue(), None)) => {
      musicPlayer ! MusicPlayer.Play(songEntry.song)
      goto(Active) using Status(Queue(), Some(songEntry))
    }
    case Event(Suspend, Status(Queue(), None)) => goto(Suspended)
    case Event(Activate, Status(Queue(), None)) => stay
    case Event(MusicPlayer.SongFinished(_), _) => stay
  }
  
  when(Suspended) {
    case Event(Suspend, _) => stay
    case Event(Append(song), Status(songs, None)) => stay using Status(songs enqueue song, None)
    case Event(Activate, Status(Queue(), None)) => goto(Waiting)
    case Event(Activate, Status(songs: Queue[SongEntry], None)) => {
      setupSong(songs)
    }
    case Event(MusicPlayer.SongFinished(_), _) => stay
  }
  
  whenUnhandled {
    case Event(GetStatus, status: Status) => {
      sender ! StreamStatus(status.nextSongs, status.current, this.stateName)
      stay
    }
  }
  
  def setupSong(songs: Queue[SongEntry]): MusicStream.this.State = {
    val (next, rest) = songs.dequeue
    musicPlayer ! MusicPlayer.Play(next.song)
    goto(Active) using Status(rest, Some(next))
  }
  
  initialize
}