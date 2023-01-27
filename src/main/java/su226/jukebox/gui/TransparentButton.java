package su226.jukebox.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import su226.jukebox.Mod;

public class TransparentButton extends Button {
  private static final ResourceLocation ICONS_ID = new ResourceLocation(Mod.ID, "textures/gui/icons.png");
  public boolean selected;
  private int iconX = -1;
  private int iconY = -1;
  
  public TransparentButton(int x, int y, int w, int h, ITextComponent text, IPressable clicked) {
    super(x, y, w, h, text, clicked, null);
  }

  public TransparentButton setIcon(int x, int y) {
    this.iconX = x;
    this.iconY = y;
    return this;
  }
  
  @Override
  public void renderButton(MatrixStack mat, int mouseX, int mouseY, float deltaT) {
    boolean hover = this.isHovered();
    Minecraft mc = Minecraft.getInstance();
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    Tessellator tess = Tessellator.getInstance();
    BufferBuilder buf = tess.getBuilder();
    GL11.glColor4f(0, 0, 0, hover ? 0.3f : 0);
    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
    buf.vertex(this.x, this.y + this.height, 0).endVertex();
    buf.vertex(this.x + this.width, this.y + this.height, 0).endVertex();
    buf.vertex(this.x + this.width, this.y, 0).endVertex();
    buf.vertex(this.x, this.y, 0).endVertex();
    tess.end();
    GL11.glColor3f(1, 1, 1);
    if (this.selected) {
      buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
      buf.vertex(this.x, this.y + this.height, 0).endVertex();
      buf.vertex(this.x + this.width, this.y + this.height, 0).endVertex();
      buf.vertex(this.x + this.width, this.y + this.height - 1, 0).endVertex();
      buf.vertex(this.x, this.y + this.height - 1, 0).endVertex();
      tess.end();
    }
    RenderSystem.disableBlend();
    RenderSystem.enableTexture();
    if (this.iconX != -1) {
      RenderSystem.enableBlend();
      mc.getTextureManager().bind(ICONS_ID);
      buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
      int x1 = this.x + this.width / 2 - 4, x2 = x1 + 8, y1 = this.y + this.height / 2 - 4, y2 = y1 + 8;
      float u1 = this.iconX / 4f, u2 = u1 + .25f, v1 = this.iconY / 4f, v2 = v1 + .25f;
      buf.vertex(x1, y2, 0).uv(u1, v2).endVertex();
      buf.vertex(x2, y2, 0).uv(u2, v2).endVertex();
      buf.vertex(x2, y1, 0).uv(u2, v1).endVertex();
      buf.vertex(x1, y1, 0).uv(u1, v1).endVertex();
      tess.end();
      RenderSystem.disableBlend();
    } else {
      Widget.drawCenteredString(mat, mc.font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xffffff);
    }
  }
}
