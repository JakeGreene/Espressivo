package ca.jakegreene.espressivo

import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media
import scalafx.application.JFXApp
import scalafx.scene.Scene

object AudioPlayer extends JFXApp {
  val resource = getClass.getResource("/resources/keeper.mp3")
  val media = new Media(resource.toString)
  val mediaPlayer = new MediaPlayer(media)
  mediaPlayer.play()

  stage = new JFXApp.PrimaryStage {
    title = "Audio Player 1"
    scene = new Scene(200, 200)
  }
  
}