package ca.jakegreene.espressivo

import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.ask
import spray.http.MediaTypes._
import spray.json._
import spray.routing._
import akka.util.Timeout
import scala.concurrent.duration._
import reflect.ClassTag
import ca.jakegreene.espressivo.music.Song
import ca.jakegreene.espressivo.music.JukeBox
import ca.jakegreene.espressivo.music.JukeBox.GetMusicLibrary
import ca.jakegreene.espressivo.music.JukeBox.Playlist
import ca.jakegreene.espressivo.music.SongId
import ca.jakegreene.espressivo.music.SongEntry
import scala.collection.immutable.ListSet

case class BasicResponse(msg: String)

object IdImplicits {
   implicit def Int2SongId(num: Int) = SongId(num)
}

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val responseFormat = jsonFormat1(BasicResponse)
  implicit val songIdFormat = jsonFormat1(SongId)
  implicit val songDescFormat = jsonFormat4(SongDescription)
}

case class SongDescription(id: SongId, name: String, artist: String, album: String)

object HttpServer {
  def describe(entry: SongEntry): SongDescription = SongDescription(entry.id, entry.song.title, entry.song.artist, entry.song.album)
}

class HttpServer(player: ActorRef) extends Actor with HttpService {
  import MyJsonProtocol._
  import IdImplicits._
  import HttpServer._
  import JukeBox._
  import spray.httpx.SprayJsonSupport._
  
  def actorRefFactory = context
  import context.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val myRoute =
    path("") {
      get {
        complete(BasicResponse("Hello, World"))
      }
    } ~
      path("songs") {
        get {
          complete {
           (player ? GetMusicLibrary).mapTo[Music].map(lib => lib.songs.map(entry => describe(entry)))
          }
        }
      } ~
      path("songs" / IntNumber) { songId =>
        get {
          complete {
            (player ? GetSong(songId)).mapTo[SongEntry].map(entry => describe(entry))
          }
        }
      } ~
      path("play" / IntNumber) { songId =>
        get {
          complete {
            player ! Play(SongId(songId))
            BasicResponse("Media Playing")
          }
        }
      } ~
      path("stop") {
        complete {
          player ! Stop
          BasicResponse("Stopping Media")
        }
      } ~
      path("pause") {
        complete {
          player ! Pause
          BasicResponse("Paused Media")
        }
      }
      
  def receive = runRoute(myRoute)
}