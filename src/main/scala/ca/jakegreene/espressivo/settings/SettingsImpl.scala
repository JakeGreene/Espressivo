package ca.jakegreene.espressivo.settings

import akka.actor.Extension
import com.typesafe.config.Config
import java.io.File
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem

class SettingsImpl(config: Config) extends Extension {
  val MusicRoot: File = new File(config.getString("espressivo.music.root"))    
}

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {
  override def lookup = Settings
  override def createExtension(system: ExtendedActorSystem) = new SettingsImpl(system.settings.config)
}