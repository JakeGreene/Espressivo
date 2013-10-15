package ca.jakegreene.espressivo

import akka.actor.Actor
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media

case class Play(song: Song)

class JukeBox extends Actor {
  
  val resource = getClass.getResource("/resources/keeper.mp3")
  val media = new Media(resource.toString)
  val mediaPlayer = new MediaPlayer(media)
  
  def receive = {
    case Play(song) => {
      mediaPlayer.play()
    }
  }
}