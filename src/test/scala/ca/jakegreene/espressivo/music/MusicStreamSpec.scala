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

class MusicStreamSpec extends TestKit(ActorSystem("MusicStreamSpec")) with WordSpecLike with Matchers with BeforeAndAfterAll with MockitoSugar {
  "A MusicStream" should {
    // Transition from Ready
    "Ready -> Waiting given Activate with no songs to play" in {
     fail()
    }
    "move to Active if told to Activate while Ready and there are songs to play" in {
      fail()
    }
    "stay in Ready if told to Append" in {
      fail()
    }
    "stay in Ready if told to Suspend" in {
      fail()
    }
    "stay in Ready if told a Song has finished" in {
      //This may result in an error
      fail()
    }
    // Transition from Waiting
    "move to Active if told to Append while in Waiting" in {
      fail()
    }
    "move to Suspended if told to Suspend while in Waiting" in {
      fail()
    }
    "stay in Waiting if told to Activate" in {
      fail()
    }
    "stay in Waiting if told a Song has finished" in {
     fail()
    }
    // Transition from Active
    "stay in Active if told to Append" in {
      fail()
    }
    "stay in Active if told to Activate" in {
      fail()
    }
    "move to Suspended if told to Suspend whil in Active" in {
      fail()
    }
    "stay in Active if told a Song is finished and there are more songs" in {
      fail()
    }
    "move to Waiting if told a Song is finished and there are no more songs" in {
      fail()
    }
    // Transition from Suspended
    "stay in Suspended if told to Suspend" in {
      fail()
    }
    "stay in Suspended if told to Append" in {
      fail()
    }
    "move to Active if told to Activate and there is a song to play" in {
      fail()
    }
    "move to Waiting if told to Activate and there is no song to play" in {
      fail()
    }
    "stay in Suspended if told a Song is finished" in {
      fail()
    }
  }
}