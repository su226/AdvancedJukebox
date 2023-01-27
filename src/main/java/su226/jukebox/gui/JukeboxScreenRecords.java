package su226.jukebox.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.item.MusicDiscItem;
import su226.jukebox.JukeboxTE;
import su226.jukebox.Mod;
import su226.jukebox.musics.RecordMusic;

public class JukeboxScreenRecords extends JukeboxScreen {
  private MusicList musicList;

  public JukeboxScreenRecords(JukeboxTE te) {
    super(te);
  }

  @Override
  protected String getName() {
    return "records";
  }

  @Override
  protected void init() {
    this.musicList = new MusicList(this.minecraft, this.width, this.height, 20, this.height - 20, 38);
    // for (Item item : ItemTags.MUSIC_DISCS.getValues()) // Not every mod add tag for its records.
    for (MusicDiscItem item : Mod.DISC_ITEMS) {
      RecordMusic music = new RecordMusic(item);
      this.musicList.addMusic(music, (x, y) -> {
        music.loadUploadInfo().thenAccept(track -> {
          this.te.requestAdd(music);
        });
      }, (text, hover) -> {
        if (!hover) {
          return text;
        }
        return this.te.contains(music) ? ADDED_TEXT : ADD_TEXT;
      });
    }
    this.addWidget(this.musicList);
    super.init();
  }

  @Override
  public void render(MatrixStack mat, int mouseX, int mouseY, float deltaT) {
    this.renderBackground(mat);
    this.musicList.render(mat, mouseX, mouseY, deltaT);
    super.render(mat, mouseX, mouseY, deltaT);
  }
}
