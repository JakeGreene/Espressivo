package ca.jakegreene.espressivo

import org.scalatest.Matchers
import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import scalafx.scene.media.Media
import akka.actor.FSM.SubscribeTransitionCallBack
import akka.actor.FSM.CurrentState
import org.scalatest.mock.MockitoSugar
import akka.actor.FSM.Transition
import scalafx.scene.media.MediaPlayer
import org.mockito.Mockito._
import ca.jakegreene.espressivo.music.Song
import ca.jakegreene.espressivo.music.SongController

class MusicPlayerSpec extends TestKit(ActorSystem("MusicPlayerSpec")) with WordSpecLike with Matchers with ImplicitSender with MockitoSugar {
	"A MusicPlayer" should {
	  "immedietly start playing a song when told to Play" in {
	    val musicPlayerRef = TestActorRef(new MusicPlayer)
	    musicPlayerRef ! SubscribeTransitionCallBack(self)
	    expectMsg(CurrentState(musicPlayerRef, Ready))
	    val song = mock[Song]
	    val controller = mock[SongController]
	    when(song.createController()).thenReturn(controller)
	    musicPlayerRef ! MusicPlayer.Play(song)
	    expectMsg(Transition(musicPlayerRef, Ready, Playing))
	    verify(controller, times(1)).play()
	  }
	}
}