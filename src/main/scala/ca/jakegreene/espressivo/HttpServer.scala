package ca.jakegreene.espressivo

import spray.routing._
import spray.http._
import spray.json._
import MediaTypes._
import akka.actor.ActorSystem
import akka.actor.Actor

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
	  }
}