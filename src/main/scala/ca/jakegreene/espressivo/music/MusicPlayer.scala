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
  case object NoSong extends Data

  sealed trait Request
  case class Play(song: Song)
  case object Stop
  case object Pause
}

class MusicPlayer extends Actor with FSM[MusicPlayer.State, MusicPlayer.Data] {
  import MusicPlayer._
  startWith(Ready, Uninitialized)

  when(Ready) {
    case Event(Play(song), Uninitialized) => {
      val controller = song.createController()
      controller.play()
      goto(Playing) using CurrentSong(controller)
    }
  }

  when(Playing) {
    case Event(Play(newSong), CurrentSong(oldSongController)) => {
      // Trying to Play the current song should not restart it
      if (newSong equals oldSongController.song) {
        stay
      } else {
        oldSongController.stop()
        val newController = newSong.createController()
        newController.play()
        stay using CurrentSong(newController)
      }
    }
    case Event(Stop, data: CurrentSong) => {
      data.song.stop()
      goto(Stopped) using NoSong
    }
    case Event(Pause, data: CurrentSong) => {
      data.song.pause()
      goto(Paused) using data
    }
  }

  when(Stopped) {
    case Event(Play(song), NoSong) => {
      val controller = song.createController()
      controller.play()
      goto(Playing) using CurrentSong(controller)
    }
    case _ => stay
  }

  when(Paused) {
    case Event(Play(song), CurrentSong(controller)) => {
      if (song equals controller.song) {
        controller.play()
        goto(Playing) using CurrentSong(controller)
      } else {
        val newController = song.createController()
        newController.play()
        goto(Playing) using CurrentSong(newController)
      }
    }
    case Event(Stop, CurrentSong(controller)) => {
      controller.stop()
      goto(Stopped) using NoSong
    }
    case _ => stay
  }

  initialize
}