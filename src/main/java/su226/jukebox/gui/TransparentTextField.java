package su226.jukebox.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;

public class TransparentTextField extends TextFieldWidget {
  private final FontRenderer font = Minecraft.getInstance().font;
  private Runnable onEnter;
  private boolean drawPlaceholder;

  public TransparentTextField(FontRenderer font, int x, int y, int w, int h, ITextComponent msg) {
    super(font, x, y, w, h, msg);
    this.setBordered(false);
  }

  public void setOnEnter(Runnable onEnter) {
    this.onEnter = onEnter;
  }

  public void setDrawPlaceholder(boolean drawPlaceholder) {
    this.drawPlaceholder = drawPlaceholder;
  }

  @Override
  public boolean keyPressed(int key, int x, int y) {
    if (this.canConsumeInput() && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && this.onEnter != null) {
      this.onEnter.run();
      return true;
    }
    return super.keyPressed(key, x, y);
  }

  @Override
  public void renderButton(MatrixStack mat, int mouseX, int mouseY, float deltaT) {
    GL11.glColor4f(0, 0, 0, 0.3f);
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    Tessellator tess = Tessellator.getInstance();
    BufferBuilder buf = tess.getBuilder();
    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
    buf.vertex(this.x, this.y + this.height, 0).endVertex();
    buf.vertex(this.x + this.width, this.y + this.height, 0).endVertex();
    buf.vertex(this.x + this.width, this.y, 0).endVertex();
    buf.vertex(this.x, this.y, 0).endVertex();
    tess.end();
    RenderSystem.disableBlend();
    RenderSystem.enableTexture();
    int offsetY = (this.height - 8) / 2;
    this.width -= 8;
    this.x += 4;
    this.y += offsetY;
    super.renderButton(mat, mouseX, mouseY, deltaT);
    if (this.drawPlaceholder && this.getValue().isEmpty()) {
      this.font.drawShadow(mat, this.getMessage(), this.x, this.y, 0x808080);
    }
    this.y -= offsetY;
    this.x -= 4;
    this.width += 8;
  }
}
