package li.cil.im.client.renderer

import java.nio.ByteBuffer

import cpw.mods.fml.common.ObfuscationReflectionHelper
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import li.cil.im.IsoMap
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.culling.Frustrum
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.{RenderGlobal, OpenGlHelper, RenderHelper}
import net.minecraft.entity.Entity
import org.lwjgl.opengl.{GL11, GL20, GL30}
import org.lwjgl.util.glu.Project
import scala.collection.convert.WrapAsScala._

object Minimap {
  private val resX = 1024
  private val resY = 1024

  var zoom = 0.5
  var size = 100
  var relPosX = 0.0
  var relPosY = 0.0
  var alpha = 0.8

  private val frameBuffer = GL30.glGenFramebuffers()
  GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer)

  private val colorBuffer = GL11.glGenTextures()
  GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorBuffer)
  GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, resX, resY, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, null: ByteBuffer)
  GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
  GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

  private val depthBuffer = GL30.glGenRenderbuffers()
  GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer)
  GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, resX, resY)

  GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer)
  GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorBuffer, 0)

  GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
  GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0)
  GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)

  var rendering = false

  def shouldRender = Minecraft.getMinecraft.theWorld != null && !rendering && size > 0 && alpha > 0

  @SubscribeEvent
  def onRenderWorldLastEvent(e: TickEvent.RenderTickEvent) {
    if (e.phase == TickEvent.Phase.END && shouldRender) try {
      rendering = true

      GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)

      val mc = Minecraft.getMinecraft
      mc.renderViewEntity = mc.thePlayer

      OpenGlHelper.func_153171_g(GL30.GL_FRAMEBUFFER, frameBuffer)
      GL11.glViewport(0, 0, resX, resY)
      GL11.glClearColor(0, 0, 0, 0)
      GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0)
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
      GL11.glEnable(GL11.GL_TEXTURE_2D)
      GL11.glEnable(GL11.GL_CULL_FACE)
      GL11.glEnable(GL11.GL_DEPTH_TEST)
      GL11.glEnable(GL11.GL_ALPHA_TEST)
      GL11.glShadeModel(GL11.GL_FLAT)
      GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f)
      GL11.glEnable(GL11.GL_BLEND)
      RenderHelper.disableStandardItemLighting()

      val oldY = mc.thePlayer.posY
      val oldLastTickPosY = mc.thePlayer.lastTickPosY
      mc.thePlayer.posY = 256
      mc.thePlayer.lastTickPosY = 256

      GL11.glMatrixMode(GL11.GL_PROJECTION)
      GL11.glPushMatrix()
      GL11.glLoadIdentity()
      GL11.glOrtho(-10 / zoom, 10 / zoom, -10 / zoom, 10 / zoom, -300, 300)
      Project.gluLookAt(1, 2, 1, 0, 0, 0, 0, 1, 0)
      GL11.glMatrixMode(GL11.GL_MODELVIEW)
      GL11.glPushMatrix()
      GL11.glLoadIdentity()
      GL11.glTranslated(0, 256 - (oldLastTickPosY + (oldY - oldLastTickPosY) * e.renderTickTime), 0)

      val frustum = new Frustrum()
      frustum.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
      mc.renderGlobal.clipRenderersByFrustum(frustum, e.renderTickTime)
      mc.getTextureManager.bindTexture(TextureMap.locationBlocksTexture)

      for (pass <- 0 to 1) {
        mc.renderGlobal.sortAndRender(mc.thePlayer, pass, e.renderTickTime)
      }

      mc.thePlayer.posY = oldY
      mc.thePlayer.lastTickPosY = oldLastTickPosY

      GL11.glMatrixMode(GL11.GL_PROJECTION)
      GL11.glPopMatrix()
      GL11.glMatrixMode(GL11.GL_MODELVIEW)
      GL11.glPopMatrix()

      mc.getFramebuffer.bindFramebuffer(true)

      GL11.glEnable(GL11.GL_BLEND)
      GL11.glColor4d(1, 1, 1, alpha)

      val res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight)
      val posX = relPosX * (res.getScaledWidth - size)
      val posY = relPosY * (res.getScaledHeight - size)

      GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorBuffer)
      GL11.glBegin(GL11.GL_QUADS)
      GL11.glTexCoord2f(0, 0)
      GL11.glVertex2d(posX, posY + size)
      GL11.glTexCoord2f(1, 0)
      GL11.glVertex2d(posX + size, posY + size)
      GL11.glTexCoord2f(1, 1)
      GL11.glVertex2d(posX + size, posY)
      GL11.glTexCoord2f(0, 1)
      GL11.glVertex2d(posX, posY)
      GL11.glEnd()

      GL11.glPopAttrib()

    }
    catch {
      case t: Throwable => IsoMap.log.warn("Oh noes, it borked!", t)
    }
    finally {
      rendering = false
    }
  }
}
