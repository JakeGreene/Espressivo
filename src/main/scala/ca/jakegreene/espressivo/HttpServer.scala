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
  implicit val songFormat = jsonFormat3(Song)
}

class HttpServer(player: ActorRef) extends Actor with HttpService {
  import MyJsonProtocol._
  import spray.httpx.SprayJsonSupport._
  
  def actorRefFactory = context
  val songsById = Map((1 -> Song("cry me a river", "ella", 1)), (2 -> Song("sing, sing, sing", "benny goodman", 2)))

  val myRoute =
    path("") {
      get {
        complete(BasicResponse("Hello, World"))
      }
    } ~
      path("songs") {
        get {
          complete(songsById.values)
        }
      } ~
      path("songs" / IntNumber) { songId =>
        get {
          complete(songsById(songId))
        }
      } ~
      path("play") {
        get {
          complete {
            player ! Play(Song("keeper", "dunno", 1))
            println("Playing Media")
            BasicResponse("Media Playing")
          }
        }
      }
      
  def receive = runRoute(myRoute)
}