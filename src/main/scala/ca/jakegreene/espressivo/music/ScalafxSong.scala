package ca.jakegreene.espressivo.music

import scala.collection.JavaConversions.asScalaSet

import org.joda.time.Duration

import scalafx.scene.media.Media
import scalafx.scene.media.MediaPlayer

class ScalafxSong(val media: Media) extends Song {
  def duration = Duration.millis(media.duration().toMillis().asInstanceOf[Long])
  def metadata = {
    val metadata = media.metadata.entrySet()
    // For the time being we will only have string metadata
    metadata.map((entry) => (entry.getKey(), entry.getValue().toString)).toMap
  }
  def createController() = new ScalafxSongController(this)
}

class ScalafxSongController(val song: ScalafxSong) extends SongController {
  val mediaPlayer = new MediaPlayer(song.media)
  def play() = mediaPlayer.play()
  def pause() = mediaPlayer.pause()
  def stop() = mediaPlayer.stop()
  def onSongEnd(op: Song => Unit) = { mediaPlayer.onEndOfMedia = op(song) } // execution of op(song) will be delayed until the song ends
}