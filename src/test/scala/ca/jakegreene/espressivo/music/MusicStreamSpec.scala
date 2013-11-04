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

class MusicStreamSpec extends TestKit(ActorSystem("MusicStreamSpec")) with WordSpecLike with Matchers with BeforeAndAfterAll with MockitoSugar {
  
  implicit val timeout = Duration(1, SECONDS)
  
  "A MusicStream" should {
    // Transition from Ready
    "Ready -> Waiting given Activate with no songs to play" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Waiting)
    }
    "Ready -> Active given Activate with songs to play" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      stream.setState(MusicStream.Ready, MusicStream.Songs(List(mock[Song])), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Active)
    }
    "stay in Ready given Append" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      val song = mock[Song]
      stream.receive(MusicStream.Append(song))
      stream.stateName should be (MusicStream.Ready)
    }
    "stay in Ready given Suspend" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      stream.receive(MusicStream.Suspend)
      stream.stateName should be (MusicStream.Ready)
    }
    "stay in Ready given a Song has finished" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      val song = mock[Song]
      stream.receive(MusicPlayer.SongFinished(song))
      stream.stateName should be (MusicStream.Ready)
    }
    // Transition from Waiting
    "Waiting -> Active given Append" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      val song = mock[Song]
      stream.setState(MusicStream.Waiting, MusicStream.Songs(List()), timeout, None)
      stream.receive(MusicStream.Append(song))
      stream.stateName should be (MusicStream.Active)
    }
    "Waiting -> Suspended given Suspend" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      stream.setState(MusicStream.Waiting, MusicStream.Songs(List()), timeout, None)
      stream.receive(MusicStream.Suspend)
      stream.stateName should be (MusicStream.Suspended)
    }
    "stay in Waiting given Activate" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      stream.setState(MusicStream.Waiting, MusicStream.Songs(List()), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Waiting)
    }
    "stay in Waiting given a Song has finished" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      stream.setState(MusicStream.Waiting, MusicStream.Songs(List()), timeout, None)
      val song = mock[Song]
      stream.receive(MusicPlayer.SongFinished(song))
      stream.stateName should be (MusicStream.Waiting)
    }
    // Transition from Active
    "stay in Active given Append" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      val song = mock[Song]
      stream.setState(MusicStream.Active, MusicStream.Songs(List(song)), timeout, None)
      val newSong = mock[Song]
      stream.receive(MusicStream.Append(newSong))
      stream.stateName should be (MusicStream.Active)
    }
    "stay in Active given Activate" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      val song = mock[Song]
      stream.setState(MusicStream.Active, MusicStream.Songs(List(song)), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Active)
    }
    "Active -> Suspended given Suspend" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      val song = mock[Song]
      stream.setState(MusicStream.Active, MusicStream.Songs(List(song)), timeout, None)
      stream.receive(MusicStream.Suspend)
      stream.stateName should be (MusicStream.Suspended)
    }
    "stay in Active if told a Song is finished and there are more songs" in {
      fail()
    }
    "move to Waiting if told a Song is finished and there are no more songs" in {
      fail()
    }
    // Transition from Suspended
    "stay in Suspended given Suspend" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      stream.setState(MusicStream.Suspended, MusicStream.Songs(Nil), timeout, None)
      stream.receive(MusicStream.Suspend)
      stream.stateName should be (MusicStream.Suspended)
    }
    "stay in Suspended given Append" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      stream.setState(MusicStream.Suspended, MusicStream.Songs(Nil), timeout, None)
      val song = mock[Song]
      stream.receive(MusicStream.Append(song))
      stream.stateName should be (MusicStream.Suspended)
    }
    "Suspended -> Active given Activate with a song to play" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      val song = mock[Song]
      stream.setState(MusicStream.Suspended, MusicStream.Songs(List(song)), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Active)
    }
    "Suspended -> Waiting given Activate with no song to play" in {
      val player = mock[ActorRef]
      val stream = TestFSMRef(new MusicStream(player))
      stream.setState(MusicStream.Suspended, MusicStream.Songs(Nil), timeout, None)
      stream.receive(MusicStream.Activate)
      stream.stateName should be (MusicStream.Waiting)
    }
    "stay in Suspended if told a Song is finished" in {
      fail()
    }
  }
}