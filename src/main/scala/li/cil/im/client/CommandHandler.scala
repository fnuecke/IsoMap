package li.cil.im.client

import cpw.mods.fml.common.event.FMLServerStartingEvent
import li.cil.im.client.renderer.Minimap
import net.minecraft.command.{CommandBase, ICommandSender}

import scala.collection.convert.WrapAsJava._
import scala.collection.mutable

object CommandHandler {
  def register(e: FMLServerStartingEvent) {
    e.registerServerCommand(MapAlphaCommand)
    e.registerServerCommand(MapPositionCommand)
    e.registerServerCommand(MapSizeCommand)
    e.registerServerCommand(MapZoomCommand)
  }

  object MapAlphaCommand extends SimpleCommand("im_alpha") {
    override def getCommandUsage(source: ICommandSender) = name + " <number>"

    override def processCommand(source: ICommandSender, command: Array[String]) {
      if (command != null && command.length > 0) {
        Minimap.alpha = CommandBase.parseDoubleBounded(source, command(0), 0, 1)
      }
    }

    override def getRequiredPermissionLevel = 0
  }

  object MapPositionCommand extends SimpleCommand("im_position") {
    aliases += "im_pos"

    override def getCommandUsage(source: ICommandSender) = name + " <number> <number>"

    override def processCommand(source: ICommandSender, command: Array[String]) {
      if (command != null && command.length > 1) {
        Minimap.relPosX = CommandBase.parseDoubleBounded(source, command(0), 0, 1)
        Minimap.relPosY = CommandBase.parseDoubleBounded(source, command(1), 0, 1)
      }
    }

    override def getRequiredPermissionLevel = 0
  }

  object MapSizeCommand extends SimpleCommand("im_size") {
    override def getCommandUsage(source: ICommandSender) = name + " <int>"

    override def processCommand(source: ICommandSender, command: Array[String]) {
      if (command != null && command.length > 0) {
        Minimap.size = CommandBase.parseIntWithMin(source, command(0), 0)
      }
    }

    override def getRequiredPermissionLevel = 0
  }

  object MapZoomCommand extends SimpleCommand("im_zoom") {
    override def getCommandUsage(source: ICommandSender) = name + " <number>"

    override def processCommand(source: ICommandSender, command: Array[String]) {
      if (command != null && command.length > 0) {
        Minimap.zoom = CommandBase.parseDoubleBounded(source, command(0), 0.1, 1.0)
      }
    }

    override def getRequiredPermissionLevel = 0
  }

  abstract class SimpleCommand(val name: String) extends CommandBase {
    protected var aliases = mutable.ListBuffer.empty[String]

    override def getCommandName = name

    override def getCommandAliases = aliases

    override def canCommandSenderUseCommand(source: ICommandSender) = true

    override def isUsernameIndex(command: Array[String], i: Int) = false

    override def addTabCompletionOptions(source: ICommandSender, command: Array[String]) = List.empty[AnyRef]
  }

}
