package ca.jakegreene.espressivo.music

import akka.actor.Actor
import akka.pattern.ask
import akka.pattern.pipe
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.ActorLogging

case class SongId(id: Int)
case class SongEntry(id: SongId, song: Song)

object JukeBox {
  
  sealed trait Request
  case object GetMusicLibrary extends Request
  case class GetSong(id: SongId) extends Request
  case object GetStream extends Request
  case class SetLast(id: SongId) extends Request
  case object Activate extends Request
  case object Suspend extends Request
  
  sealed trait Response
  case class Music(songs: Iterable[SongEntry]) extends Response
  
  def apply(library: MusicLibrary) = new JukeBox(library)
}

class JukeBox(songLibrary: MusicLibrary) extends Actor with ActorLogging {
  import JukeBox._
  implicit val timeout = Timeout(1 seconds)
  import context.dispatcher
  val musicPlayer = context.actorOf(Props[MusicPlayer], "espressivo-player")
  val musicStream = context.actorOf(Props(new MusicStream(musicPlayer)), "espressivo-stream")
  def receive = {
    case GetMusicLibrary => sender ! Music(songLibrary.entries)
    case GetSong(id) => sender ! SongEntry(id, songLibrary(id))
    case GetStream => {
      /* Exposing 'sender' to a future introduces possible concurrency issues as the
       * value of sender can change before the future resolves.
       * Store the current sender to remove any possible synchronization errors
       */
      val currentSender = sender
      val futureStatus = (musicStream ? MusicStream.GetStatus).mapTo[MusicStream.StreamStatus]
      futureStatus pipeTo currentSender
    }
    case SetLast(id) => {
      musicStream ! MusicStream.Append(SongEntry(id, songLibrary(id)))
    }
    case Activate => musicStream ! MusicStream.Activate
    case Suspend => musicStream ! MusicStream.Suspend
  }
}