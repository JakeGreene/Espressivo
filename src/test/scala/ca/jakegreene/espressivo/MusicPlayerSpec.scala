package ca.jakegreene.espressivo

import scala.concurrent.duration.DurationInt

import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.scalatest.mock.MockitoSugar

import akka.actor.ActorSystem
import akka.actor.FSM.CurrentState
import akka.actor.FSM.SubscribeTransitionCallBack
import akka.actor.FSM.Transition
import akka.testkit.ImplicitSender
import akka.testkit.TestActorRef
import akka.testkit.TestFSMRef
import akka.testkit.TestKit
import ca.jakegreene.espressivo.MusicPlayer._
import ca.jakegreene.espressivo.music.Song
import ca.jakegreene.espressivo.music.SongController

class MusicPlayerSpec extends TestKit(ActorSystem("MusicPlayerSpec")) with WordSpecLike with Matchers with ImplicitSender with MockitoSugar {
	"A MusicPlayer" should {
	  "immedietly start playing a song when told to Play" in {
	    val musicPlayerRef = prepareMusicPlayer()
	    val (song, controller) = prepareSongAndController()
	    musicPlayerRef ! MusicPlayer.Play(song)
	    expectMsg(Transition(musicPlayerRef, Ready, Playing))
	    verify(controller, times(1)).play()
	  }
	  "stop playing a song when given another song to play" in {
	    val musicPlayerRef = prepareMusicPlayer()
	    val (initialSong , initialController) = prepareSongAndController()
	    musicPlayerRef ! MusicPlayer.Play(initialSong)
	    expectMsg(Transition(musicPlayerRef, Ready, Playing))
	    val (newSong, newController) = prepareSongAndController()
	    musicPlayerRef ! MusicPlayer.Play(newSong)
	    verify(initialController, times(1)).stop()
	  }
	  "start playing a new song when another song is currently playing" in {
	    val musicPlayerRef = prepareMusicPlayer()
	    val (initialSong , initialController) = prepareSongAndController()
	    musicPlayerRef ! MusicPlayer.Play(initialSong)
	    expectMsg(Transition(musicPlayerRef, Ready, Playing))
	    val (newSong, newController) = prepareSongAndController()
	    musicPlayerRef ! MusicPlayer.Play(newSong)
	    verify(newController, times(1)).play()
	  }
	  "discard an old song when given a new one" in {
	    val musicFsm = prepareFsmMusicPlayer()
	    val (initialSong , initialController) = prepareSongAndController()
	    musicFsm ! MusicPlayer.Play(initialSong)
	    expectMsg(Transition(musicFsm, Ready, Playing))
	    val (newSong, newController) = prepareSongAndController()
	    musicFsm ! MusicPlayer.Play(newSong)
	    musicFsm.stateData should be (CurrentSong(newController))
	  }
	  "not restart a song if it receives Play(current song)" in {
	    val musicFsm = prepareFsmMusicPlayer()
	    val (song , initialController) = prepareSongAndController()
	    musicFsm ! MusicPlayer.Play(song)
	    expectMsg(Transition(musicFsm, Ready, Playing))
	    val newController = mock[SongController]
	    setController(song, newController)
	    musicFsm ! MusicPlayer.Play(song)
	    musicFsm.stateData should be (CurrentSong(initialController))
	  }
	  "stop a playing song when given Stop" in {
	    val (_ , controller) = prepareSongAndController()
	    val musicFsm = preparePlayingPlayer(controller)
	    musicFsm ! MusicPlayer.Stop
	    expectMsg(Transition(musicFsm, Playing, Stopped))
	    verify(controller, times(1)).stop()
	  }
	  "puase a playing song when given Pause" in {
	    val (_ , controller) = prepareSongAndController()
	    val musicFsm = preparePlayingPlayer(controller)
	    musicFsm ! MusicPlayer.Pause
	    expectMsg(Transition(musicFsm, Playing, Paused))
	    verify(controller, times(1)).pause()
	  }
	  
	}
	
	private def prepareMusicPlayer(): TestActorRef[MusicPlayer] = {
	  val musicPlayerRef = TestActorRef(new MusicPlayer)
	  musicPlayerRef ! SubscribeTransitionCallBack(self)
	  expectMsg(CurrentState(musicPlayerRef, Ready))
	  return musicPlayerRef
	}
	
	private def prepareFsmMusicPlayer(): TestFSMRef[State, Data, MusicPlayer] = {
	  val fsmRef = TestFSMRef(new MusicPlayer)
	  fsmRef ! SubscribeTransitionCallBack(self)
	  expectMsg(CurrentState(fsmRef, Ready))
	  return fsmRef
	}
	
	private def preparePlayingPlayer(controller: SongController): TestFSMRef[State, Data, MusicPlayer] = {
	  val fsmRef = prepareFsmMusicPlayer()
	  fsmRef ! Play(controller.song)
	  expectMsg(Transition(fsmRef, Ready, Playing))
	  return fsmRef
	}
	
	private def prepareSongAndController(): Tuple2[Song, SongController] = {
	  val song = mock[Song]
	  val controller = mock[SongController]
	  setController(song, controller)
	  return (song, controller)
	}
	
	private def setController(song: Song, controller: SongController) {
	  when(song.createController()).thenReturn(controller)
	  when(controller.song).thenReturn(song)
	}
}