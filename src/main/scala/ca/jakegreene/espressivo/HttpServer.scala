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

case class BasicResponse(msg: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val responseFormat = jsonFormat1(BasicResponse)
  implicit val songIdFormat = jsonFormat1(SongId)
  implicit val songDescFormat = jsonFormat2(SongDescription)
}

case class SongDescription(id: SongId, name: String)

object HttpServer {
  def describe(entry: SongEntry): SongDescription = SongDescription(entry.id, entry.song.title)
}

class HttpServer(player: ActorRef) extends Actor with HttpService {
  import MyJsonProtocol._
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
           (player ? GetMusicLibrary).mapTo[MusicLibrary].map(lib => lib.songs.map(entry => describe(entry)))
          }
        }
      } ~
      path("songs" / IntNumber) { songId =>
        get {
          complete {
            (player ? GetSong(SongId(songId))).mapTo[SongEntry].map(entry => describe(entry))
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
      }
      
  def receive = runRoute(myRoute)
}