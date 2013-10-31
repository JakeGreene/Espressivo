package ca.jakegreene.espressivo.music

import org.joda.time.Duration

/**
 * An immutable representation of a song
 */
trait Song {
  def duration: Duration
  def title: String = {
    try {
      metadata("title")  
    } catch {
      case _:Throwable => "No Name"
    }
  }
  def album: String = {
    try {
      metadata("album")  
    } catch {
      case _:Throwable => "No Name"
    }
  }
  def artist: String = {
    try {
      metadata("artist")  
    } catch {
      case _:Throwable => "No Name"
    }
  }
  def metadata: Map[String, String]
  def createController(): SongController
}

/**
 * A SongController is responsible for
 * controlling the playing of a Song
 */
trait SongController {
  def play()
  def pause()
  def stop()
  def song: Song
  def onSongEnd_=(op: => Unit): Unit
}