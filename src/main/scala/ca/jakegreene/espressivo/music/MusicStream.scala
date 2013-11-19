package ca.jakegreene.espressivo.music

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.ListBuffer
import akka.actor.ActorLogging
import akka.actor.FSM

object MusicStream {
  sealed trait Request
  case object Activate extends Request
  case object Suspend extends Request
  case class Append(song: Song) extends Request
  
  sealed trait State
  case object Ready extends State
  case object Active extends State
  case object Suspended extends State
  case object Waiting extends State
  
  sealed trait Data
  case class Songs(newestFirst: List[Song], current: Option[Song]) extends Data
}

import MusicStream._
class MusicStream(musicPlayer: ActorRef) extends Actor with ActorLogging with FSM[State, Data] {
  
  musicPlayer ! MusicPlayer.ListenForSongEnd(self)
  startWith(Ready, Songs(Nil, None))
  
  when(Ready) {
    case Event(Activate, Songs(Nil, None)) => goto (Waiting) using Songs(Nil, None)
    case Event(Activate, Songs(song :: rest, None)) => {
      musicPlayer ! MusicPlayer.Play(song)
      goto (Active) using Songs(rest, Some(song))
    }
    case Event(Append(song), Songs(songs, None)) => stay using Songs(song +: songs, None) // Newest song at the head of the list
    case Event(Suspend, _) => stay
    case Event(MusicPlayer.SongFinished, _) => stay
    case msg => stay
  }
  
  when(Active) {
    case Event(Append(song), Songs(newestFirst, None)) => stay using Songs(song +: newestFirst, None)
    case Event(Activate, _) => stay
    case Event(Suspend, _) => goto(Suspended)
    case Event(MusicPlayer.SongFinished(song), Songs(Nil, Some(current))) if song equals current => goto(Waiting) using Songs(Nil, None)
    case Event(MusicPlayer.SongFinished(song), Songs(next :: rest, Some(current))) if song equals current => {
      musicPlayer ! MusicPlayer.Play(next)
      stay using Songs(rest, Some(next))
    }
    case _ => stay
  }
  
  when(Waiting) {
    case Event(Append(song), Songs(Nil, None)) => {
      musicPlayer ! MusicPlayer.Play(song)
      goto(Active) using Songs(Nil, Some(song))
    }
    case Event(Suspend, Songs(Nil, None)) => goto(Suspended)
    case Event(Activate, Songs(Nil, None)) => stay
    case Event(MusicPlayer.SongFinished, _) => stay
    case _ => stay
  }
  
  when(Suspended) {
    case Event(Suspend, _) => stay
    case Event(Append(song), Songs(newestFirst, None)) => stay using Songs(song +: newestFirst, None)
    case Event(Activate, Songs(song :: rest, None)) => {
      musicPlayer ! MusicPlayer.Play(song)
      goto(Active) using Songs(rest, Some(song))
    }
    case Event(Activate, Songs(Nil, None)) => goto(Waiting)
    case Event(MusicPlayer.SongFinished, _) => stay
    case _ => stay
  }
  
  initialize
}