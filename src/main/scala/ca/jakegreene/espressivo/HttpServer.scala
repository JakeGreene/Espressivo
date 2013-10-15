package ca.jakegreene.espressivo

import spray.routing._
import spray.http._
import spray.json._
import MediaTypes._
import akka.actor.ActorSystem
import akka.actor.Actor
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media
import akka.actor.ActorRef

case class BasicResponse(msg: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val responseFormat = jsonFormat1(BasicResponse)
  implicit val songIdFormat = jsonFormat1(SongId)
  implicit val songFormat = jsonFormat3(SongDescription)
}

class HttpServer(player: ActorRef) extends Actor with HttpService {
  import MyJsonProtocol._
  import spray.httpx.SprayJsonSupport._
  
  def actorRefFactory = context

  val myRoute =
    path("") {
      get {
        complete(BasicResponse("Hello, World"))
      }
    } ~
      path("songs") {
        get {
          //complete(songsById.values)
          complete(BasicResponse("Not Available"))
        }
      } ~
      path("songs" / IntNumber) { songId =>
        get {
          //complete(songsById(songId))
          complete(BasicResponse("Not Available"))
        }
      } ~
      path("play" / IntNumber) { songId =>
        get {
          complete {
            player ! Play(SongId(songId))
            BasicResponse("Media Playing")
          }
        }
      }
      
  def receive = runRoute(myRoute)
}