package ca.jakegreene.espressivo

case class SongDescription(name: String, band: String, id: SongId)
case class SongId(id: Int)