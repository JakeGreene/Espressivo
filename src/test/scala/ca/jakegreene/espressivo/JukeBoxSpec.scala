package ca.jakegreene.espressivo

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.scalatest.mock.MockitoSugar
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import akka.util.Timeout
import ca.jakegreene.espressivo.music.Song
import scala.concurrent.Await
import ca.jakegreene.espressivo.JukeBox._

class JukeBoxSpec extends TestKit(ActorSystem("JukeBoxSpec")) with WordSpecLike with Matchers with BeforeAndAfterAll with MockitoSugar {
  
  implicit val timeout = Timeout(1 seconds)
  
  "A JukeBox" should {
    "provide the full library when queried" in {
      val libSize = 2
      val songsById = createLibrary(libSize)
      val jukebox = TestActorRef(new JukeBox(songsById))
      val futureLibrary = (jukebox ? GetMusicLibrary).mapTo[MusicLibrary]
      val returnedLibrary = Await.result(futureLibrary, 1 seconds)
      returnedLibrary.songs.size should be (libSize)
      val songEntries = songsById.map(tuple => JukeBox.entry(tuple))
      returnedLibrary.songs should be (songEntries)
    }
    "provide a song when queried by ID" in {
      val libSize = 2
      val songsById = createLibrary(libSize)
      val jukebox = TestActorRef(new JukeBox(songsById))
      val songEntries = songsById.map(tuple => JukeBox.entry(tuple))
      for (i <- 1 to libSize) {
        val futureEntry = (jukebox ? GetSong(SongId(i))).mapTo[SongEntry]
        val entry = Await.result(futureEntry, 1 seconds)
        songEntries should contain (entry)
      }
    }
  }
  
  private def createLibrary(size: Int): Map[SongId, Song] = {
    val tuples = for {
      id <- (1 to size)
      song = mock[Song]
    } yield (SongId(id) -> song)
    return tuples.toMap
  }
}