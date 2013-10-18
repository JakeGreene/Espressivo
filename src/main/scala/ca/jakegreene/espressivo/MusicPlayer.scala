package ca.jakegreene.espressivo

import akka.actor.FSM
import akka.actor.Actor
import scalafx.scene.media.Media
import scalafx.scene.media.MediaPlayer
import ca.jakegreene.espressivo.music.SongController
import ca.jakegreene.espressivo.music.Song

sealed trait State
case object Playing extends State
case object Stopped extends State
case object Paused extends State
case object Ready extends State

sealed trait Data
case object Uninitialized extends Data
case class CurrentSong(song: SongController) extends Data

object MusicPlayer {
  sealed trait Request
  case class Play(song: Song)
  case object Stop
  case object Pause
}

class MusicPlayer extends Actor with FSM[State, Data] {
  import MusicPlayer._
  startWith(Ready, Uninitialized)
  
  when(Ready) {
    case Event(Play(song), Uninitialized) => {
      println(s"Playing $song")
      val controller = song.createController()
      controller.play()
      goto(Playing) using CurrentSong(controller)
    }
  }
  
  when(Playing) {
    case _ => stay
  }
  
  when(Stopped) {
    case _ => stay
  }
  
  when(Paused) {
    case _ => stay
  }
  
  initialize
}