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
import ca.jakegreene.espressivo.music.JukeBox.Playlists
import ca.jakegreene.espressivo.music.JukeBox.PlaylistId
import ca.jakegreene.espressivo.music.SongId
import ca.jakegreene.espressivo.music.SongEntry
import scala.collection.immutable.ListSet

case class BasicResponse(msg: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val responseFormat = jsonFormat1(BasicResponse)
  implicit val songIdFormat = jsonFormat1(SongId)
  implicit val songDescFormat = jsonFormat4(SongDescription)
  implicit val playlistIdFormat = jsonFormat1(PlaylistId)
  implicit object PlaylistFormat extends RootJsonFormat[Playlist] {
    def write(p: Playlist) = JsObject(
      "name" -> JsString(p.name),
      "songs" -> JsArray(p.songs.map(id => JsNumber(id.id)))
    )
    
    def read(value: JsValue) = {
      value.asJsObject.getFields("name", "songs") match {
        case Seq(JsString(name), JsArray(songs)) => {
          val ids = songs.map(value => value match { 
            case JsNumber(num) => SongId(num.intValue)
            case _ => throw new DeserializationException("Expected a list of intereger IDs")
          })
          Playlist(name, ids) 
        }
        case _ => throw new DeserializationException("Expected Playlist(name, ids)")
      }
    }
  }
  //implicit val playlistFormat = jsonFormat2(Playlist)
  implicit val playlistsFormat = jsonFormat1(Playlists)
}

case class SongDescription(id: SongId, name: String, artist: String, album: String)

object HttpServer {
  def describe(entry: SongEntry): SongDescription = SongDescription(entry.id, entry.song.title, entry.song.artist, entry.song.album)
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
           (player ? GetMusicLibrary).mapTo[Music].map(lib => lib.songs.map(entry => describe(entry)))
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
      } ~
      path("pause") {
        complete {
          player ! Pause
          BasicResponse("Paused Media")
        }
      } ~
      path("playlists") {
        get {
          complete((player ? GetPlaylists).mapTo[Playlists])
        } ~
        post {
          entity(as[Playlist]) { playlist =>
            complete {
              (player ? StorePlaylist(playlist)).mapTo[Playlists]
            }
          }
        }
      } ~
      path("playlists" / IntNumber) { playlistId =>
        get {
          complete {
            (player ? GetPlaylist(PlaylistId(playlistId))).mapTo[Playlist]
          }
        }
      }
      
  def receive = runRoute(myRoute)
}