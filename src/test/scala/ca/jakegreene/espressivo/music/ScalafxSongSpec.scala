package ca.jakegreene.espressivo.music

import org.scalatest.mock.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.WordSpec

/**
 * Most of the functionality of ScalafxSongs cannot currently be tested because Media cannot be mocked
 */
class ScalafxSongSpec extends WordSpec with Matchers with BeforeAndAfterAll with MockitoSugar {
  //Throws a null pointer exception because media is null and MediaPlayer does not accept null Media
//  "A ScalafxSong" should {
//    "create a unique ScalafxSongController" in {
//      val song = new ScalafxSong(null)
//      val controllerA = song.createController()
//      val controllerB = song.createController()
//      controllerA should not be (controllerB)
//    }
//  }
}