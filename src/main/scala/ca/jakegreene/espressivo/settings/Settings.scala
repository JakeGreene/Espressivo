package ca.jakegreene.espressivo.settings

import akka.actor.Extension
import com.typesafe.config.Config
import java.io.File
import com.typesafe.config._
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem
import akka.actor.ActorSystem

class Settings(config: Config) extends Extension {
  val musicRoot: File = new File(config.getString("espressivo.music.root"))
  val host: String = config.getString("espressivo.server.host")
  val port: Int = config.getInt("espressivo.server.port")
}

object Settings {
  def apply(system: ActorSystem) = new Settings(system.settings.config)
  def apply(system: ActorSystem, userConfig: Config) = new Settings(userConfig.withFallback(system.settings.config))
}