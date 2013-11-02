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
    case _ => stay
  }
  
  when(Active) {
    case _ => stay
  }
  
  initialize
}