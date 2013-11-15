package ca.jakegreene.espressivo.music

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
import ca.jakegreene.espressivo.music.MusicPlayer._

class MusicPlayerSpec extends TestKit(ActorSystem("MusicPlayerSpec")) with WordSpecLike with Matchers with ImplicitSender with MockitoSugar {
	"A MusicPlayer" should {
	  // MusicPlayer.Ready State Transition tests
	  "immediately start playing a song when told to Play" in {
	    val musicPlayerRef = prepareMusicPlayer()
	    val (song, controller) = prepareSongAndController()
	    musicPlayerRef ! MusicPlayer.Play(song)
	    expectMsg(Transition(musicPlayerRef, Ready, Playing))
	    verify(controller, times(1)).play()
	  }
	  // MusicPlayer.Playing State Transition tests
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
	  "puase a playing song when given Pause" in {
	    val (_ , controller) = prepareSongAndController()
	    val musicFsm = preparePlayingPlayer(controller)
	    musicFsm ! MusicPlayer.Pause
	    expectMsg(Transition(musicFsm, Playing, Paused))
	    verify(controller, times(1)).pause()
	  }
	  "stop a playing song when given Stop" in {
	    val (_ , controller) = prepareSongAndController()
	    val musicFsm = preparePlayingPlayer(controller)
	    musicFsm ! MusicPlayer.Stop
	    expectMsg(Transition(musicFsm, Playing, Stopped))
	    verify(controller, times(1)).stop()
	  }
	  "move to Ready when song ends in Playing" in {
	    val (song , controller) = prepareSongAndController()
	    val musicFsm = preparePlayingPlayer(controller)
	    musicFsm ! MusicPlayer.SongFinished(song)
	    expectMsg(Transition(musicFsm, Playing, Ready))
	  }
	  // MusicPlayer.Stopped State Transition tests
	  "play a new song when told to Play in Stopped" in {
	    val (_ , controller) = prepareSongAndController()
	    val musicFsm = prepareStoppedPlayer(controller)
	    val (song , newController) = prepareSongAndController()
	    musicFsm ! MusicPlayer.Play(song)
	    expectMsg(Transition(musicFsm, Stopped, Playing))
	    verify(newController, times(1)).play()
	  }
	  "do nothing when given Stop in Stopped" in {
	    val (_ , controller) = prepareSongAndController()
	    val musicFsm = prepareStoppedPlayer(controller)
	    musicFsm ! MusicPlayer.Stop
	    assert(musicFsm.stateName equals MusicPlayer.Stopped)
	  }
	  "do nothing when given Pause in Stopped" in {
	    val (_ , controller) = prepareSongAndController()
	    val musicFsm = prepareStoppedPlayer(controller)
	    musicFsm ! MusicPlayer.Pause
	    assert(musicFsm.stateName equals MusicPlayer.Stopped)
	  }
	  "move to Ready when song ends in Stopped" in {
	    val (song , controller) = prepareSongAndController()
	    val musicFsm = prepareStoppedPlayer(controller)
	    musicFsm ! MusicPlayer.SongFinished(song)
	    expectMsg(Transition(musicFsm, Playing, Ready))
	  }
	  // MusicPlayer.Paused State Transition tests
	  "resume a paused song when given Play(current song) in Paused" in {
	    val (song , controller) = prepareSongAndController()
	    val musicFsm = preparePausedPlayer(controller)
	    musicFsm ! MusicPlayer.Play(song)
	    expectMsg(Transition(musicFsm, Paused, Playing))
	    //One for initial play, once for resume
	    verify(controller, times(2)).play()
	  }
	  "play a different song when given Play(diff song) in Paused" in {
	    val (song , controller) = prepareSongAndController()
	    val musicFsm = preparePausedPlayer(controller)
	    val (newSong , newController) = prepareSongAndController()
	    musicFsm ! MusicPlayer.Play(newSong)
	    expectMsg(Transition(musicFsm, Paused, Playing))
	    verify(controller, times(1)).play()
	    verify(newController, times(1)).play()
	  }
	  "stop the current song when given Stop in Paused" in {
	    val (song , controller) = prepareSongAndController()
	    val musicFsm = preparePausedPlayer(controller)
	    musicFsm ! MusicPlayer.Stop
	    expectMsg(Transition(musicFsm, Paused, Stopped))
	    verify(controller, times(1)).stop()
	  }
	  "do nothing when give Pause in Paused" in {
	    val (song , controller) = prepareSongAndController()
	    val musicFsm = preparePausedPlayer(controller)
	    musicFsm ! MusicPlayer.Pause
	    assert(musicFsm.stateName equals MusicPlayer.Paused)
	  }
	  "move to Ready when song ends in Paused" in {
	    val (song , controller) = prepareSongAndController()
	    val musicFsm = preparePausedPlayer(controller)
	    musicFsm ! MusicPlayer.SongFinished(song)
	    expectMsg(Transition(musicFsm, Playing, Ready))
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
	
	private def prepareStoppedPlayer(controller: SongController): TestFSMRef[State, Data, MusicPlayer] = {
	  val fsmRef = prepareFsmMusicPlayer()
	  controller.stop()
	  fsmRef.setState(Stopped, CurrentSong(controller), 1 second, None)
	  expectMsg(Transition(fsmRef, Ready, Stopped))
	  return fsmRef
	}
	
	private def preparePausedPlayer(controller: SongController): TestFSMRef[State, Data, MusicPlayer] = {
	  val fsmRef = preparePlayingPlayer(controller)
	  fsmRef ! MusicPlayer.Pause
	  expectMsg(Transition(fsmRef, Playing, Paused))
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