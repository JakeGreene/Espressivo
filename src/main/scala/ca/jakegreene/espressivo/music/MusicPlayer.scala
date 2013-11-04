package ca.jakegreene.espressivo.music

import akka.actor.Actor
import akka.actor.FSM

object MusicPlayer {
  sealed trait State
  case object Playing extends State
  case object Stopped extends State
  case object Paused extends State
  case object Ready extends State

  sealed trait Data
  case object Uninitialized extends Data
  case class CurrentSong(song: SongController) extends Data

  sealed trait Request
  case class Play(song: Song) extends Request
  case object Stop extends Request
  case object Pause extends Request
  
  sealed trait Output
  case class SongFinished(song: Song) extends Output
}

class MusicPlayer extends Actor with FSM[MusicPlayer.State, MusicPlayer.Data] {
  import MusicPlayer._
  startWith(Ready, Uninitialized)
  
  private def playSong(song: Song, currentController: SongController): MusicPlayer.this.State = {
    if (currentController.song equals song) {
      currentController.play()
      goto(Playing) using CurrentSong(currentController)
    }
    else {
      currentController.stop()
      val newController = song.createController()
      newController.play()
      goto(Playing) using CurrentSong(newController)
    }
  }

  when(Ready) {
    case Event(Play(song), Uninitialized) => {
      val controller = song.createController()
      controller.play()
      goto(Playing) using CurrentSong(controller)
    }
  }

  when(Playing) {
    case Event(Play(newSong), CurrentSong(oldSongController)) => {
      playSong(newSong, oldSongController)
    }
    case Event(Stop, data: CurrentSong) => {
      data.song.stop()
      goto(Stopped)
    }
    case Event(Pause, data: CurrentSong) => {
      data.song.pause()
      goto(Paused) using data
    }
  }

  when(Stopped) {
    case Event(Play(song), CurrentSong(oldController)) => {
      playSong(song, oldController)
    }
    case _ => stay
  }

  when(Paused) {
    case Event(Play(song), CurrentSong(controller)) => {
      playSong(song, controller)
    }
    case Event(Stop, CurrentSong(controller)) => {
      controller.stop()
      goto(Stopped)
    }
    case _ => stay
  }

  initialize
}