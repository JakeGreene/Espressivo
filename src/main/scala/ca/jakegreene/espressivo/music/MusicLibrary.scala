package ca.jakegreene.espressivo.music

import java.io.File
import java.net.URI

import scala.Array.canBuildFrom

object MusicLibrary {
  def apply(lib: Map[SongId, Song]) = new MusicLibrary(lib)
  def entry(id: SongId, song: Song): SongEntry = SongEntry(id, song)
  def entry(tuple: Tuple2[SongId, Song]): SongEntry = (entry _).tupled(tuple)
  def inDirectory(dir: File, songCreator: URI => Song): MusicLibrary = {
    val locations = findAllMusicLocations(dir)
    MusicLibrary(loadMusic(locations, songCreator))
  }
  
  private def findAllMusicLocations(root: File): Seq[URI] = {
    val files = root.listFiles().filter(_.isFile())
    val directories = root.listFiles().filter(_.isDirectory())
    return filesToURI(files) ++ directories.flatMap(findAllMusicLocations(_))
  }
  
  private def filesToURI(files: Seq[File]): Seq[URI] = {
    files.map(_.toURI())
  }
  
  private def loadMusic(songLocations: Seq[URI], songCreator: URI => Song): Map[SongId, Song] = {
    songLocations.foreach(println)
    (for {
      (location, id) <- songLocations.zipWithIndex
      if supported(location)
      song = songCreator(location)
      songId = SongId(id)
    } yield (songId, song)).toMap
  }
  
  private def supported(song: URI): Boolean = {
    val suffixStart = song.getRawPath().lastIndexOf(".")
    val suffix = song.getRawPath().substring(suffixStart+1)
    Seq("mp3", "wav", "aif", "aiff") contains suffix
  }
}

class MusicLibrary(library: Map[SongId, Song]) {
  import MusicLibrary._
  def apply(id: SongId) = library(id)
  def entries: Iterable[SongEntry] = library.map(tuple => entry(tuple))
}