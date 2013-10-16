package ca.jakegreene.espressivo

import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}
import org.scalatest.Matchers
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await


class JukeBoxSpec extends TestKit(ActorSystem("JukeBoxSpec")) with WordSpecLike with Matchers with BeforeAndAfterAll {
  
  "A JukeBox" should {
    "be able to return the full library" in {
      val originalLibrary = createLibrary(2)
      val allDescriptions = originalLibrary.values.map(v => v.description)
      val jukeBoxRef = TestActorRef(new JukeBox(originalLibrary))
      implicit val timeout = Timeout(5)
      val libraryFuture = (jukeBoxRef ? GetMusicLibrary).mapTo[MusicLibrary]
      val retrievedLibrary = Await.result(libraryFuture, 1 second).songs
      allDescriptions should equal (retrievedLibrary)
    }
    "provide access to individual song descriptions" in {
      val libSize = 2  
      val originalLibrary = createLibrary(libSize)
      val allDescriptions = originalLibrary.values.map(v => v.description)
      val jukeBoxRef = TestActorRef(new JukeBox(originalLibrary))
      implicit val timeout = Timeout(5)
      for (id <- (1 to libSize)) {
        val songFuture = (jukeBoxRef ? GetSong(SongId(id))).mapTo[SongDescription]
        val songDescription = Await.result(songFuture, 1 second)
        allDescriptions should contain (songDescription)
      }
      
    }
  }
  
  private def createLibrary(size: Int): Map[SongId, Song] = {
    val tuples = for {
      id <- (1 to size)
      desc = SongDescription(SongId(id), id.toString)
      song = Song(desc, null)
    } yield (SongId(id) -> song)
    return tuples.toMap
  }
}