package ca.jakegreene.espressivo

import spray.routing._
import spray.http._
import spray.json._
import MediaTypes._
import akka.actor.ActorSystem
import akka.actor.Actor

case class BasicResponse(msg: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val responseFormat = jsonFormat1(BasicResponse)
}

class HttpAccessControl extends Actor with EspressivoService {
	def actorRefFactory = context
	def receive = runRoute(myRoute)
}

trait EspressivoService extends HttpService {
  import MyJsonProtocol._
  import spray.httpx.SprayJsonSupport._
  
  val myRoute = 
	  path("") {
		get {
		  respondWithMediaType(`application/json`) {
		    complete(BasicResponse("Hello, World"))
		  }
		}
	}
}