package onimen.anni.hmage.module.hud;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import onimen.anni.hmage.Preferences;
import onimen.anni.hmage.module.AbstractModule;
import onimen.anni.hmage.module.hud.layout.Layout;
import onimen.anni.hmage.module.hud.layout.Layout.LayoutType;

public abstract class AbstractHUD extends AbstractModule implements InterfaceHUD {

  protected float zLevel = 0.0f;

  @Override
  public void setX(int value) {
    Preferences.setInt(getName() + ".x", value);
  }

  @Override
  public int getX() {
    return Preferences.getInt(getName() + ".x", getDefaultX());
  }

  @Override
  public void setY(int value) {
    Preferences.setInt(getName() + ".y", value);
  }

  @Override
  public int getY() {
    return Preferences.getInt(getName() + ".y", getDefaultY());
  }

  @Override
  public void setLayout(Layout layout) {
    Preferences.setInt(this.getName() + ".layout", layout.getCode());
  }

  @Override
  public Layout getLayout() {
    return Layout.getLayout(Preferences.getInt(this.getName() + ".layout", 0));
  }

  @Override
  public boolean isHorizontal() {
    return getLayout().getDirection() == LayoutType.HORIZONTAL;
  }

  @Override
  public int getComputedX(ScaledResolution sr) {
    int x = getX();
    switch (getLayout().getLayoutX()) {
    case LEFT:
      break;
    case RIGHT:
      x *= -1;
      x += sr.getScaledWidth() - getWidth();
      break;
    case CENTERX:
      x += (sr.getScaledWidth() - getWidth()) / 2;
      break;
    default:
      break;
    }
    return x;
  }

  @Override
  public int getComputedY(ScaledResolution sr) {
    int y = getY();
    switch(getLayout().getLayoutY()) {
    case TOP:
      break;
    case BOTTOM:
      y *= -1;
      y += sr.getScaledHeight() - getHeight();
      break;
    case CENTERY:
      y += (sr.getScaledHeight() - getHeight())/2;
      break;
    default:
      break;
    }
    return y;
  }

  @Override
  public void drawItem(Minecraft mc) {
    this.drawItem(mc, false);
  }

  @Override
  public List<String> getPreferenceKeys() {
    List<String> preferenceKeys = super.getPreferenceKeys();
    preferenceKeys.addAll(Lists.newArrayList("x", "y", "position"));
    return preferenceKeys;
  }

  public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
    bufferbuilder.pos((double) (x + 0), (double) (y + height), (double) this.zLevel)
        .tex((double) ((float) (textureX + 0) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F))
        .endVertex();
    bufferbuilder.pos((double) (x + width), (double) (y + height), (double) this.zLevel)
        .tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F))
        .endVertex();
    bufferbuilder.pos((double) (x + width), (double) (y + 0), (double) this.zLevel)
        .tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY + 0) * 0.00390625F))
        .endVertex();
    bufferbuilder.pos((double) (x + 0), (double) (y + 0), (double) this.zLevel)
        .tex((double) ((float) (textureX + 0) * 0.00390625F), (double) ((float) (textureY + 0) * 0.00390625F))
        .endVertex();
    tessellator.draw();
  }

  public void drawRect(int x, int y, int width, int height) {

    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();

    GlStateManager.pushMatrix();
    GlStateManager.color(0f, 0f, 0f, 0.3f);
    GlStateManager.translate(x, y, 0);
    GlStateManager.glBegin(GL11.GL_QUADS);
    GlStateManager.glVertex3f(0, height, 0);
    GlStateManager.glVertex3f(width, height, 0);
    GlStateManager.glVertex3f(width, 0, 0);
    GlStateManager.glVertex3f(0, 0, 0);
    GlStateManager.glEnd();
    GlStateManager.popMatrix();

    GlStateManager.disableBlend();
    GlStateManager.enableTexture2D();
  }

  public boolean isInside(ScaledResolution sr, int x, int y) {
    int left = this.getComputedX(sr);
    int top = this.getComputedY(sr);
    return (x >= left && x <= left + getWidth() && y >= top && y <= top + getHeight());
  }
}
