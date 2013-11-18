package ca.jakegreene.espressivo.music

import akka.actor.Actor
import akka.actor.FSM
import akka.actor.ActorRef
import akka.actor.ActorLogging

object MusicPlayer {
  sealed trait State
  case object Playing extends State
  case object Stopped extends State
  case object Paused extends State
  case object Ready extends State

  sealed trait Data
  case object Uninitialized extends Data
  case class CurrentSong(controller: SongController) extends Data
  case object NoSong extends Data

  sealed trait Request
  case class Play(song: Song) extends Request
  case object Stop extends Request
  case object Pause extends Request
  
  sealed trait Output
  case class SongFinished(song: Song) extends Output
  
}

class MusicPlayer extends Actor with FSM[MusicPlayer.State, MusicPlayer.Data] with ActorLogging {
  import MusicPlayer._
  startWith(Ready, Uninitialized)
  
  private def playSong(song: Song, currentController: SongController): MusicPlayer.this.State = {
    if (currentController.song equals song) {
      currentController.play()
      goto(Playing) using CurrentSong(currentController)
    }
    else {
      currentController.stop()
      playNewSong(song)
    }
  }
  
  private def playNewSong(song: Song): MusicPlayer.this.State = {
     val newController = song.createController()
      newController.onSongEnd(tellSongFinished(self))
      newController.play()
      goto(Playing) using CurrentSong(newController)
  }
  
  private def tellSongFinished(actor: ActorRef)(song: Song) {
    actor ! SongFinished(song)
  }

  when(Ready) {
    case Event(Play(song), _) => {
      playNewSong(song)
    }
  }

  when(Playing) {
    case Event(Play(newSong), CurrentSong(oldSongController)) => {
      playSong(newSong, oldSongController)
    }
    case Event(Stop, data: CurrentSong) => {
      data.controller.stop()
      goto(Stopped)
    }
    case Event(Pause, data: CurrentSong) => {
      data.controller.pause()
      goto(Paused) using data
    }
  }

  when(Stopped) {
    case Event(Play(song), CurrentSong(oldController)) => {
      playSong(song, oldController)
    }
    case Event(msg: Request, _) => stay
  }

  when(Paused) {
    case Event(Play(song), CurrentSong(controller)) => {
      playSong(song, controller)
    }
    case Event(Stop, CurrentSong(controller)) => {
      controller.stop()
      goto(Stopped)
    }
    case Event(msg: Request, _) => stay
  }
  
  whenUnhandled {
    case Event(SongFinished(song), CurrentSong(controller)) if song equals controller.song => goto(Ready) using NoSong
    case Event(msg, state) => {
      log.warning(s"Unhandled Event: Received $msg with $state while ${this.stateName}")
      stay
    }
  }

  initialize
}