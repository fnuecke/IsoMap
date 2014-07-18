package li.cil.im

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLServerStartingEvent}
import cpw.mods.fml.common.{Mod, SidedProxy}
import org.apache.logging.log4j.LogManager

@Mod(modid = IsoMap.ID, name = IsoMap.Name, version = IsoMap.Version,
  modLanguage = "scala", useMetadata = true)
object IsoMap {
  final val ID = "IsoMap"

  final val Name = "Isometric Minimap"

  final val Version = "@VERSION@"

  val log = LogManager.getLogger("OpenComputers")

  @SidedProxy(clientSide = "li.cil.im.client.Proxy")
  var proxy: common.Proxy = null

  @EventHandler
  def init(e: FMLInitializationEvent) = proxy.init(e)

  @EventHandler
  def serverStart(e: FMLServerStartingEvent) = proxy.serverStart(e)
}
