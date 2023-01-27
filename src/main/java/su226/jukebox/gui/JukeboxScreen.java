package su226.jukebox.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import su226.jukebox.Blocks;
import su226.jukebox.JukeboxTE;
import su226.jukebox.Mod;

public abstract class JukeboxScreen extends Screen {
  protected static final TranslationTextComponent ADD_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".add");
  protected static final TranslationTextComponent ADDED_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".added");
  private static final List<Pair<String, Function<JukeboxTE, Screen>>> TABS = new ArrayList<>();
  protected final JukeboxTE te;

  static {
    JukeboxScreen.registerTab("playlist", JukeboxScreenPlaylist::new);
    JukeboxScreen.registerTab("records", JukeboxScreenRecords::new);
    JukeboxScreen.registerTab("netease", JukeboxScreenNetease::new);
    JukeboxScreen.registerTab("url", JukeboxScreenUrl::new);
  }

  public JukeboxScreen(JukeboxTE te) {
    super(Blocks.JUKEBOX.get().getName());
    this.te = te;
  }

  public static void registerTab(String name, Function<JukeboxTE, Screen> constructor) {
    TABS.add(Pair.of(name, constructor));
  }

  public static void openFirst(JukeboxTE te) {
    Minecraft.getInstance().setScreen(TABS.get(0).getValue().apply(te));
  }

  protected abstract String getName();

  protected int getHeaderHeight() {
    return 20;
  }

  @Override
  protected void init() {
    int x = 8;
    for (Pair<String, Function<JukeboxTE, Screen>> tab : TABS) {
      ITextComponent text = new TranslationTextComponent(String.format("gui.%s.%s", Mod.ID, tab.getKey()));
      TransparentButton button = this.addButton(new TransparentButton(x, 0, this.font.width(text) + 16, 20, text, b -> {
        Minecraft.getInstance().setScreen(tab.getValue().apply(this.te));
      }));
      if (this.getName().equals(tab.getKey())) {
        button.selected = true;
      }
      x += button.getWidth();
    }
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public void render(MatrixStack mat, int mouseX, int mouseY, float deltaT) {
    Tessellator tess = Tessellator.getInstance();
    BufferBuilder buf = tess.getBuilder();
    this.minecraft.getTextureManager().bind(AbstractGui.BACKGROUND_LOCATION);
    RenderSystem.enableDepthTest();
    RenderSystem.depthFunc(GL11.GL_ALWAYS);
    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
    int headerHeight = this.getHeaderHeight();
    buf.vertex(0, headerHeight, 0).color(64, 64, 64, 255).uv(0, headerHeight / 32f).endVertex();
    buf.vertex(this.width, headerHeight, 0).color(64, 64, 64, 255).uv(this.width / 32f, headerHeight / 32f).endVertex();
    buf.vertex(this.width, 0, 0).color(64, 64, 64, 255).uv(this.width / 32f, 0).endVertex();
    buf.vertex(0, 0, 0).color(64, 64, 64, 255).uv(0, 0).endVertex();
    tess.end();
    RenderSystem.depthFunc(GL11.GL_LEQUAL);
    RenderSystem.disableDepthTest();
    RenderSystem.enableBlend();
    GL11.glShadeModel(GL11.GL_SMOOTH);
    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
    buf.vertex(0, headerHeight + 4, 0).color(0, 0, 0, 0).uv(0, 1).endVertex();
    buf.vertex(this.width, headerHeight + 4, 0).color(0, 0, 0, 0).uv(1, 1).endVertex();
    buf.vertex(this.width, headerHeight, 0).color(0, 0, 0, 255).uv(1, 0).endVertex();
    buf.vertex(0, headerHeight, 0).color(0, 0, 0, 255).uv(0, 0).endVertex();
    tess.end();
    GL11.glShadeModel(GL11.GL_FLAT);
    RenderSystem.disableBlend();
    super.render(mat, mouseX, mouseY, deltaT);
  }
}
