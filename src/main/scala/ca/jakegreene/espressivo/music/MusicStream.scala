package ca.jakegreene.espressivo.music

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.ListBuffer
import akka.actor.ActorLogging
import akka.actor.FSM

import MusicStream._
import akka.actor.LoggingFSM

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
  case class Status(nextSongs: List[SongEntry], current: Option[SongEntry]) extends Data
}
class MusicStream(musicPlayer: ActorRef) extends Actor with ActorLogging with LoggingFSM[State, Data] {
  
  musicPlayer ! MusicPlayer.ListenForSongEnd(self)
  startWith(Ready, Status(Nil, None))
  
  when(Ready) {
    case Event(Activate, Status(Nil, None)) => goto (Waiting) using Status(Nil, None)
    case Event(Activate, Status(songEntry :: rest, None)) => {
      musicPlayer ! MusicPlayer.Play(songEntry.song)
      goto (Active) using Status(rest, Some(songEntry))
    }
    case Event(Append(song), Status(songs, None)) => stay using Status(song +: songs, None) // Newest song at the head of the list
    case Event(Suspend, _) => stay
    case Event(MusicPlayer.SongFinished(_), _) => stay
  }
  
  when(Active) {
    case Event(Append(song), Status(newestFirst, playing)) => stay using Status(song +: newestFirst, playing)
    case Event(Activate, _) => stay
    case Event(Suspend, _) => goto(Suspended)
    case Event(MusicPlayer.SongFinished(song), Status(Nil, Some(current))) if song equals current.song => goto(Waiting) using Status(Nil, None)
    case Event(MusicPlayer.SongFinished(song), Status(next :: rest, Some(current))) if song equals current.song => {
      musicPlayer ! MusicPlayer.Play(next.song)
      stay using Status(rest, Some(next))
    }
  }
  
  when(Waiting) {
    case Event(Append(songEntry), Status(Nil, None)) => {
      musicPlayer ! MusicPlayer.Play(songEntry.song)
      goto(Active) using Status(Nil, Some(songEntry))
    }
    case Event(Suspend, Status(Nil, None)) => goto(Suspended)
    case Event(Activate, Status(Nil, None)) => stay
    case Event(MusicPlayer.SongFinished(_), _) => stay
  }
  
  when(Suspended) {
    case Event(Suspend, _) => stay
    case Event(Append(song), Status(newestFirst, None)) => stay using Status(song +: newestFirst, None)
    case Event(Activate, Status(songEntry :: rest, None)) => {
      musicPlayer ! MusicPlayer.Play(songEntry.song)
      goto(Active) using Status(rest, Some(songEntry))
    }
    case Event(Activate, Status(Nil, None)) => goto(Waiting)
    case Event(MusicPlayer.SongFinished(_), _) => stay
  }
  
  whenUnhandled {
    case Event(GetStatus, status @ Status(_, _)) => {
      sender ! status
      stay
    }
  }
  
  initialize
}