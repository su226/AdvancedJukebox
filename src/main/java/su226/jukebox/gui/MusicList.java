package su226.jukebox.gui;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.util.text.ITextComponent;
import su226.jukebox.gui.MusicEntry.IconSuppiler;
import su226.jukebox.gui.MusicEntry.TextComponentSuppiler;
import su226.jukebox.musics.Music;

public class MusicList extends AbstractList<MusicEntry> {
  public MusicList(Minecraft mc, int w, int h, int listTop, int listHeignt, int itemHeight) {
    super(mc, w, h, listTop, listTop + listHeignt, itemHeight);
    this.setRenderBackground(false);
    this.setRenderTopAndBottom(false);
  }

  @Override
  public int getRowWidth() {
    return this.width - 16;
  }

  @Override
  public int getRowLeft() {
    return 8;
  }

  @Override
  public int getRowRight() {
    return this.width - 8;
  }

  @Override
  protected int getScrollbarPosition() {
    return this.width - 6;
  }

  @Override
  public void render(MatrixStack mat, int mouseX, int mouseY, float deltaT) {
    super.render(mat, mouseX, mouseY, deltaT);
  }

  public MusicEntry addMusic(Music music, BiConsumer<Integer, Integer> callback, BiFunction<ITextComponent, Boolean, ITextComponent> customText) {
    MusicEntry entry = new MusicEntry(hover -> {
      ITextComponent text = music.getName();
      return customText == null ? text : customText.apply(text, hover);
    }, hover -> music.getArtist(), hover -> music.getAlbum(), hover -> music.getIcon(), callback);
    this.addEntry(entry);
    music.loadDisplayInfo();
    return entry;
  }

  public void addSpecial(TextComponentSuppiler primaryLine, TextComponentSuppiler secondaryLine, TextComponentSuppiler tertiaryLine, IconSuppiler icon, BiConsumer<Integer, Integer> callback) {
    this.addEntry(new MusicEntry(primaryLine, secondaryLine, tertiaryLine, icon, callback));
  }
}
