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
  case class Songs(active: Option[Song], index: Option[Int], songs: Array[Song]) extends Data
}

import MusicStream._
class MusicStream(musicPlayer: ActorRef) extends Actor with ActorLogging with FSM[State, Data] {
  startWith(Ready, Songs(None, None, Array()))
  
  when(Ready) {
    case Event(Activate, Songs(a, i, Array())) => {
      goto (Waiting) using Songs(a, i, Array())
    }
    case Event(Activate, Songs(a, i, songs)) if songs.length > 0 => goto (Active) using Songs(a, i, songs)
    case msg => stay
  }
  
  when(Active) {
    case _ => stay
  }
  
  when(Waiting) {
    case _ => stay
  }
  
  initialize
}