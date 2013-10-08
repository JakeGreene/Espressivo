package ca.jakegreene.espressivo

import akka.actor.ActorSystem
import akka.io.IO
import akka.actor.Props
import spray.can.Http

object Espressivo extends App {
  implicit val system = ActorSystem("espressivo-system")
  val service = system.actorOf(Props[HttpAccessControl], "espressivo-server")
  val host = "localhost"
  val port = 8080
  IO(Http) ! Http.Bind(service, interface = host, port = port)
}