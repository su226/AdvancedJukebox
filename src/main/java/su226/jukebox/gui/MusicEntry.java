package su226.jukebox.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList.AbstractListEntry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import su226.jukebox.icons.IIcon;

public class MusicEntry extends AbstractListEntry<MusicEntry> {
  @FunctionalInterface
  public static interface TextComponentSuppiler {
    ITextComponent get(boolean hover);
  }
  @FunctionalInterface
  public static interface IconSuppiler {
    IIcon get(boolean hover);
  }

  private static final String ELLIPSIS = "...";
  private static int ellipsisWidth;
  protected final Minecraft mc;
  private final TextComponentSuppiler primaryLine;
  private final TextComponentSuppiler secondaryLine;
  private final TextComponentSuppiler tertiaryLine;
  private final IconSuppiler icon;
  private final BiConsumer<Integer, Integer> callback;
  private final List<Button> buttons;

  public MusicEntry(TextComponentSuppiler primaryLine, TextComponentSuppiler secondaryLine, TextComponentSuppiler tertiaryLine, IconSuppiler icon, BiConsumer<Integer, Integer> callback) {
    this.mc = Minecraft.getInstance();
    this.primaryLine = primaryLine;
    this.secondaryLine = secondaryLine;
    this.tertiaryLine = tertiaryLine;
    this.icon = icon;
    this.callback = callback;
    this.buttons = new ArrayList<>();
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    for (Button button : this.buttons) {
      if (button.isMouseOver(mouseX, mouseY)) {
        Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));;
        button.onPress();
        return true;
      }
    }
    if (this.callback != null) {
      Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));;
      this.callback.accept((int)mouseX, (int)mouseY);
      return true;
    }
    return false;
  }

  public void addButton(Button button) {
    this.buttons.add(button);
  }

  public void render(MatrixStack mat, int index, int y, int x, int w, int h, int mouseX, int mouseY, boolean hover, float deltaT) {
    if (hover) {
      GL11.glColor4f(0, 0, 0, 0.3f);
      RenderSystem.disableTexture();
      RenderSystem.enableBlend();
      Tessellator tess = Tessellator.getInstance();
      BufferBuilder buf = tess.getBuilder();
      buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
      buf.vertex(x, y + h, 0).endVertex();
      buf.vertex(x + w, y + h, 0).endVertex();
      buf.vertex(x + w, y, 0).endVertex();
      buf.vertex(x, y, 0).endVertex();
      tess.end();
      RenderSystem.disableBlend();
      RenderSystem.enableTexture();
      int buttonX = x + w;
      for (int i = this.buttons.size() - 1; i >= 0; i--) {
        Button button = this.buttons.get(i);
        int buttonW = button.getWidth();
        buttonX -= buttonW;
        button.x = buttonX;
        button.y = y;
        button.render(mat, mouseX, mouseY, deltaT);
      }
    }
    if (this.icon != null) {
      this.icon.get(hover).render(mat, x, y, h);
    }
    if (this.primaryLine != null) {
      this.mc.font.drawShadow(mat, this.primaryLine.get(hover), x + h + 4, y + 4, 0xffffff);
    }
    if (this.secondaryLine != null) {
      this.mc.font.drawShadow(mat, this.secondaryLine.get(hover), x + h + 4, y + 13, 0x888888);
    }
    if (this.tertiaryLine != null) {
      this.drawEllipsis(mat, this.tertiaryLine.get(hover), x + h + 4, y + 22, w - x - h - 8, 0x888888);
    }
  }

  private void drawEllipsis(MatrixStack mat, ITextProperties text, int x, int y, int w, int color) {
    if (ellipsisWidth == 0) {
      ellipsisWidth = this.mc.font.width(ELLIPSIS);
    }
    if (this.mc.font.width(text) > w) {
      text = this.mc.font.substrByWidth(text, w - ellipsisWidth);
      this.mc.font.drawShadow(mat, text.getString(), x, y, color);
      this.mc.font.drawShadow(mat, ELLIPSIS, x + this.mc.font.width(text), y, color);
    } else {
      this.mc.font.drawShadow(mat, text.getString(), x, y, color);
    }
  }
}
