package ca.jakegreene.espressivo

import spray.routing._
import spray.http._
import spray.json._
import MediaTypes._
import akka.actor.ActorSystem
import akka.actor.Actor
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media

case class BasicResponse(msg: String)
case class Song(name: String, band: String, id: Int)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val responseFormat = jsonFormat1(BasicResponse)
  implicit val songFormat = jsonFormat3(Song)
}

class HttpServer extends Actor with EspressivoService {
	def actorRefFactory = context
	def receive = runRoute(myRoute)
}

trait EspressivoService extends HttpService {
  import MyJsonProtocol._
  import spray.httpx.SprayJsonSupport._
  
  val songsById = Map((1 -> Song("cry me a river", "ella", 1)), (2 -> Song("sing, sing, sing", "benny goodman", 2)))
  val resource = getClass.getResource("/resources/keeper.mp3")
  val media = new Media(resource.toString)
  val mediaPlayer = new MediaPlayer(media)
  
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
	              mediaPlayer.play()
	              println("Playing Media")
	              BasicResponse("Media Playing")
	          }
	      }
	  } ~
	  path("pause") {
	      get {
	          complete {
	            mediaPlayer.pause()
	            BasicResponse("Media Paused")
	          }
	      }
	  } ~
	  path("status") {
	      get {
	        complete(BasicResponse(mediaPlayer.status().name()))
	      }
	  }
}