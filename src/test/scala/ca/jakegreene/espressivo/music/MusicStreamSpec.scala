package ca.jakegreene.espressivo.music

import akka.testkit.TestKit
import org.scalatest.mock.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.testkit.TestActorRef
import akka.testkit.TestFSMRef
import scala.concurrent.duration._
import akka.testkit.TestProbe
import scala.collection.immutable.Queue

class MusicStreamSpec extends TestKit(ActorSystem("MusicStreamSpec")) with WordSpecLike with Matchers with BeforeAndAfterAll with MockitoSugar {
  
  implicit val timeout = Duration(1, SECONDS)
  
  "A MusicStream" should {
    // Transition from Ready
    "Ready -> Waiting given Activate with no songs to play" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Waiting)
    }
    "Ready -> Active given Activate with songs to play" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val songEntry = mockSongEntry(SongId(0))
      stream.setState(MusicStream.Ready, MusicStream.Status(Queue(songEntry), None), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Active)
      probe.expectMsg(MusicPlayer.ListenForSongEnd(stream))
      probe.expectMsg(MusicPlayer.Play(songEntry.song))
    }
    "stay in Ready given Append" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val songEntry = mockSongEntry(SongId(0))
      stream.receive(MusicStream.Append(songEntry))
      stream.stateName should be (MusicStream.Ready)
    }
    "stay in Ready given Suspend" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      stream.receive(MusicStream.Suspend)
      stream.stateName should be (MusicStream.Ready)
    }
    "stay in Ready given a Song has finished" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val songEntry = mockSongEntry(SongId(0))
      stream.receive(MusicPlayer.SongFinished(songEntry.song))
      stream.stateName should be (MusicStream.Ready)
    }
    // Transition from Waiting
    "Waiting -> Active given Append" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val songEntry = mockSongEntry(SongId(0))
      stream.setState(MusicStream.Waiting, MusicStream.Status(Queue(), None), timeout, None)
      stream.receive(MusicStream.Append(songEntry))
      stream.stateName should be (MusicStream.Active)
      probe.expectMsg(MusicPlayer.ListenForSongEnd(stream))
      probe.expectMsg(MusicPlayer.Play(songEntry.song))
    }
    "Waiting -> Suspended given Suspend" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      stream.setState(MusicStream.Waiting, MusicStream.Status(Queue(), None), timeout, None)
      stream.receive(MusicStream.Suspend)
      stream.stateName should be (MusicStream.Suspended)
    }
    "stay in Waiting given Activate" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      stream.setState(MusicStream.Waiting, MusicStream.Status(Queue(), None), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Waiting)
    }
    "stay in Waiting given a Song has finished" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      stream.setState(MusicStream.Waiting, MusicStream.Status(Queue(), None), timeout, None)
      val songEntry = mockSongEntry(SongId(0))
      stream.receive(MusicPlayer.SongFinished(songEntry.song))
      stream.stateName should be (MusicStream.Waiting)
    }
    // Transition from Active
    "stay in Active given Append" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val currentSongEntry = mockSongEntry(SongId(1))
      stream.setState(MusicStream.Active, MusicStream.Status(Queue(), Some(currentSongEntry)), timeout, None)
      val newSongEntry = mockSongEntry(SongId(2))
      stream.receive(MusicStream.Append(newSongEntry))
      stream.stateName should be (MusicStream.Active)
    }
    "stay in Active given Activate" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val songEntry = mockSongEntry(SongId(0))
      val currentSongEntry = mockSongEntry(SongId(1))
      stream.setState(MusicStream.Active, MusicStream.Status(Queue(songEntry), Some(currentSongEntry)), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Active)
    }
    "Active -> Suspended given Suspend" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val currentSongEntry = mockSongEntry(SongId(0))
      stream.setState(MusicStream.Active, MusicStream.Status(Queue(), Some(currentSongEntry)), timeout, None)
      stream.receive(MusicStream.Suspend)
      stream.stateName should be (MusicStream.Suspended)
    }
    "stay in Active if told a Song is finished and there are more songs" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val playingSong = mockSongEntry(SongId(1))
      val nextSong = mockSongEntry(SongId(0))
      stream.setState(MusicStream.Active, MusicStream.Status(Queue(nextSong), Some(playingSong)), timeout, None)
      stream.receive(MusicPlayer.SongFinished(playingSong.song))
      stream.stateName should be (MusicStream.Active)
      probe.expectMsg(MusicPlayer.ListenForSongEnd(stream))
      probe.expectMsg(MusicPlayer.Play(nextSong.song))
    }
    "move to Waiting if told a Song is finished and there are no more songs" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val playingSong = mockSongEntry(SongId(0))
      stream.setState(MusicStream.Active, MusicStream.Status(Queue(), Some(playingSong)), timeout, None)
      stream.receive(MusicPlayer.SongFinished(playingSong.song))
      stream.stateName should be (MusicStream.Waiting)
    }
    // Transition from Suspended
    "stay in Suspended given Suspend" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      stream.setState(MusicStream.Suspended, MusicStream.Status(Queue(), None), timeout, None)
      stream.receive(MusicStream.Suspend)
      stream.stateName should be (MusicStream.Suspended)
    }
    "stay in Suspended given Append" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      stream.setState(MusicStream.Suspended, MusicStream.Status(Queue(), None), timeout, None)
      val songEntry = mockSongEntry(SongId(0))
      stream.receive(MusicStream.Append(songEntry))
      stream.stateName should be (MusicStream.Suspended)
    }
    "Suspended -> Active given Activate with a new song to play" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val songEntry = mockSongEntry(SongId(0))
      stream.setState(MusicStream.Suspended, MusicStream.Status(Queue(songEntry), None), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Active)
      probe.expectMsg(MusicPlayer.ListenForSongEnd(stream))
      probe.expectMsg(MusicPlayer.Play(songEntry.song))
    }
    "Suspended -> Active given Activate with a song paused" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val newSong = mockSongEntry(SongId(0))
      val pausedSong = mockSongEntry(SongId(1))
      stream.setState(MusicStream.Suspended, MusicStream.Status(Queue(newSong), Some(pausedSong)), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Active)
      probe.expectMsg(MusicPlayer.ListenForSongEnd(stream))
      probe.expectMsg(MusicPlayer.Play(pausedSong.song))
    }
    "Suspended -> Waiting given Activate with no song to play" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      stream.setState(MusicStream.Suspended, MusicStream.Status(Queue(), None), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Waiting)
    }
    "stay in Suspended if told a Song is finished" in {
      val (stream, player, probe) = createStreamPlayerAndProbe()
      val song = mock[Song]
      stream.setState(MusicStream.Suspended, MusicStream.Status(Queue(), None), timeout, None)
      stream.receive(MusicPlayer.SongFinished(song))
      stream.stateName should be (MusicStream.Suspended)
    }
  }
  
  private def createStreamPlayerAndProbe(): Tuple3[TestFSMRef[MusicStream.State, MusicStream.Data, MusicStream], ActorRef, TestProbe] = {
    val probe = new TestProbe(system)
    val player = probe.ref
    val stream = TestFSMRef(new MusicStream(player))
    (stream, player, probe)
  }
  
  private def mockSongEntry(id: SongId): SongEntry = {
    val song = mock[Song]
    SongEntry(id, song)
  }
}