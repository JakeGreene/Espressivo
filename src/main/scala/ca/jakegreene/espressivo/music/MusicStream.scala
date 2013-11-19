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
  case class Songs(newestFirst: List[Song]) extends Data
}

import MusicStream._
class MusicStream(musicPlayer: ActorRef) extends Actor with ActorLogging with FSM[State, Data] {
  
  musicPlayer ! MusicPlayer.ListenForSongEnd(self)
  startWith(Ready, Songs(Nil))
  
  when(Ready) {
    case Event(Activate, Songs(Nil)) => goto (Waiting) using Songs(Nil)
    case Event(Activate, Songs(song :: rest)) => {
      musicPlayer ! MusicPlayer.Play(song)
      goto (Active) using Songs(rest)
    }
    case Event(Append(song), Songs(songs)) => stay using Songs(song +: songs) // Newest song at the head of the list
    case Event(Suspend, _) => stay
    case Event(MusicPlayer.SongFinished, _) => stay
    case msg => stay
  }
  
  when(Active) {
    case Event(Append(song), Songs(newestFirst)) => stay using Songs(song +: newestFirst)
    case Event(Activate, _) => stay
    case Event(Suspend, _) => goto(Suspended)
    case Event(MusicPlayer.SongFinished(song), Songs(Nil)) => goto(Waiting) using Songs(Nil)
    case Event(MusicPlayer.SongFinished(song), Songs(next :: rest)) => {
      musicPlayer ! MusicPlayer.Play(next)
      stay using Songs(rest)
    }
    case _ => stay
  }
  
  when(Waiting) {
    case Event(Append(song), Songs(Nil)) => {
      musicPlayer ! MusicPlayer.Play(song)
      goto(Active)
    }
    case Event(Suspend, Songs(Nil)) => goto(Suspended)
    case Event(Activate, Songs(Nil)) => stay
    case Event(MusicPlayer.SongFinished, _) => stay
    case _ => stay
  }
  
  when(Suspended) {
    case Event(Suspend, _) => stay
    case Event(Append(song), Songs(newestFirst)) => stay using Songs(song +: newestFirst)
    case Event(Activate, Songs(head :: tail)) => goto(Active)
    case Event(Activate, Songs(Nil)) => goto(Waiting)
    case Event(MusicPlayer.SongFinished, _) => stay
    case _ => stay
  }
  
  initialize
}