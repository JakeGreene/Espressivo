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
import ca.jakegreene.espressivo.music.SongId
import ca.jakegreene.espressivo.music.SongEntry
import scala.collection.immutable.ListSet
import ca.jakegreene.espressivo.music.MusicStream
import akka.actor.ActorLogging

case class BasicResponse(msg: String)

object IdImplicits {
   implicit def Int2SongId(num: Int) = SongId(num)
}

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val responseFormat = jsonFormat1(BasicResponse)
  implicit val songIdFormat = jsonFormat1(SongId)
  implicit val songDescFormat = jsonFormat4(SongDescription)
  implicit val streamDescFormat = jsonFormat4(StreamDescription)
  implicit val stateDescFormat = jsonFormat1(StateDescription)
}

/*
 * Description Case Classes represent the data to be marshalled/unmarshelled when
 * communicating with a client6
 */
case class SongDescription(id: SongId, name: String, artist: String, album: String)
case class StreamDescription(songs: Seq[SongId], current: Option[SongId], last: Option[SongId], state: String)
case class StateDescription(state: String)

object HttpServer {
  def describe(entry: SongEntry): SongDescription = SongDescription(entry.id, entry.song.title, entry.song.artist, entry.song.album)
  def describe(stream: MusicStream.StreamStatus): StreamDescription = {
    val nextSongs = stream.nextSongs.map(entry => entry.id)
    val current = stream.current.map(describe(_).id)
    StreamDescription(nextSongs, current, nextSongs.lastOption, stream.state.toString())
  }
}
 
class HttpServer(player: ActorRef) extends Actor with HttpService with ActorLogging {
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
           (player ? GetMusicLibrary).mapTo[Music].map(lib => lib.songs.map(describe(_)))
          }
        }
      } ~
      path("songs" / IntNumber) { songId =>
        get {
          complete {
            (player ? GetSong(songId)).mapTo[SongEntry].map(describe(_))
          }
        }
      } ~
      path("stream") {
        get {
          complete {
            getStatus().map(describe(_))
          }
        }
      } ~
      path("stream" / "current") {
        get {
          complete {
            getStatus().map(describe(_).current)
          }
        }
      } ~
      path("stream" / "last") {
        get {
          complete {
            getStatus().map(describe(_).last)
          }
        } ~
        put {
          entity(as[SongId]) { id =>
            complete {
              player ! JukeBox.SetLast(id)
              BasicResponse(s"Song $id appended to music stream")  
            }
          }
        }
      } ~
      path("stream" / "state") {
        get {
          complete {
            getStatus().map(status => StateDescription(describe(status).state))
          }
        }
        put {
          entity(as[StateDescription]) { stateDesc =>
            complete {
              stateDesc.state match {
                case "Active" => player ! JukeBox.Activate
                case "Suspended" => player ! JukeBox.Suspend
              }
              BasicResponse("Stream Updated")
            }  
          }
        }
      }
      
  def receive = runRoute(myRoute)
  
  def getStatus() = (player ? GetStream).mapTo[MusicStream.StreamStatus]
}