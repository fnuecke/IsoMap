package li.cil.im.client

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLServerStartingEvent}
import li.cil.im.common

class Proxy extends common.Proxy {
  override def init(e: FMLInitializationEvent) {
    FMLCommonHandler.instance.bus.register(renderer.Minimap)
  }

  override def serverStart(e: FMLServerStartingEvent) {
    CommandHandler.register(e)
  }
}
