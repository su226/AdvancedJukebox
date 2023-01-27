package su226.jukebox.gui;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;
import su226.jukebox.JukeboxTE;
import su226.jukebox.Mod;
import su226.jukebox.musics.Music;

public class JukeboxScreenPlaylist extends JukeboxScreen {
  private static final TranslationTextComponent PLAY_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".play");
  private static final TranslationTextComponent PAUSE_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".pause");
  private static final TranslationTextComponent STOP_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".stop");
  private static final TranslationTextComponent PREV_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".prev");
  private static final TranslationTextComponent NEXT_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".next");
  private static final TranslationTextComponent SEQUENTIAL_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".sequential");
  private static final TranslationTextComponent LOOP_ALL_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".loop_all");
  private static final TranslationTextComponent LOOP_SINGLE_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".loop_single");
  private static final TranslationTextComponent RANDOM_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".random");
  private static final TranslationTextComponent CLEAR_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".clear");
  private static final TranslationTextComponent REFRESH_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".refresh");
  private static final TranslationTextComponent DELETE_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".delete");
  private static final TranslationTextComponent MOVE_UP_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".move_up");
  private static final TranslationTextComponent MOVE_DOWN_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".move_down");
  private TransparentButton playButton;
  private TransparentButton modeButton;
  private MusicList musicList;
  private Slider slider;

  public JukeboxScreenPlaylist(JukeboxTE te) {
    super(te);
  }

  @Override
  protected String getName() {
    return "playlist";
  }

  @Override
  protected int getHeaderHeight() {
    return 40;
  }

  @Override
  protected void init() {
    super.init();
    this.playButton = this.addButton(new TransparentButton(8, 20, 20, 20, PLAY_TEXT, button -> {
      this.te.requestSetPaused(!this.te.isPaused());
    })).setIcon(0, 0);
    this.addButton(new TransparentButton(28, 20, 20, 20, STOP_TEXT, button -> {
      this.te.requestSetCurrent(null);
    })).setIcon(2, 0);
    this.addButton(new TransparentButton(48, 20, 20, 20, PREV_TEXT, button -> {
      this.te.requestPrev();
    })).setIcon(0, 3);
    this.addButton(new TransparentButton(68, 20, 20, 20, NEXT_TEXT, button -> {
      this.te.requestNext();
    })).setIcon(1, 3);
    this.modeButton = this.addButton(new TransparentButton(88, 20, 20, 20, SEQUENTIAL_TEXT, button -> {
      JukeboxTE.Mode[] modes = JukeboxTE.Mode.values();
      this.te.requestSetMode(modes[(this.te.getMode().ordinal() + 1) % modes.length]);
    })).setIcon(0, 2);
    this.addButton(new TransparentButton(this.width - 28, 0, 20, 20, CLEAR_TEXT, button -> {
      this.te.requestClear();
    })).setIcon(3, 0);
    this.addButton(new TransparentButton(this.width - 48, 0, 20, 20, REFRESH_TEXT, button -> {
      this.te.requestSync();
    })).setIcon(2, 3);
    this.musicList = new MusicList(this.minecraft, this.width, this.height, 40, this.height - 40, 38);
    this.addWidget(this.musicList);
    this.slider = new Slider(108, 20, this.width - 116, 20, Mod.EMPTY_TEXT, Mod.EMPTY_TEXT, 0, 0, 0, false, false, button -> {}) {
      @Override
      public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        te.requestSeek((long)slider.getValue());
      }
    };
    this.slider.setMessage(new StringTextComponent("00:00/00:00"));
    this.addWidget(this.slider);
    this.addMusic();
    this.tick();
  }

  private void addMusic() {
    this.musicList.children().clear();
    List<Music> musics = this.te.getAll();
    for (int i = 0; i < musics.size(); i++) {
      Music music = musics.get(i);
      MusicEntry entry = this.musicList.addMusic(music, (mouseX, mouseY) -> {
        this.te.requestSetCurrent(music.getIdentifier());
      }, (text, hover) -> {
        return music.getIdentifier().equals(this.te.getCurrent()) ? text.plainCopy().withStyle(TextFormatting.BOLD) : text;
      });
      entry.addButton(new TransparentButton(0, 0, 20, 20, MOVE_UP_TEXT, button -> {
        this.te.requestMove(music.getIdentifier(), true);
      }).setIcon(0, 1));
      entry.addButton(new TransparentButton(0, 0, 20, 20, MOVE_DOWN_TEXT, button -> {
        this.te.requestMove(music.getIdentifier(), false);
      }).setIcon(1, 1));
      entry.addButton(new TransparentButton(0, 0, 20, 20, DELETE_TEXT, button -> {
        this.te.requestRemove(music.getIdentifier());
      }).setIcon(3, 0));
    }
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (this.slider.dragging && this.slider.mouseReleased(mouseX, mouseY, button)) {
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public void render(MatrixStack mat, int mouseX, int mouseY, float deltaT) {
    this.renderBackground(mat);
    this.musicList.render(mat, mouseX, mouseY, deltaT);
    super.render(mat, mouseX, mouseY, deltaT);
    this.slider.render(mat, mouseX, mouseY, deltaT);
  }

  @Override
  public void tick() {
    if (this.te.clearScreenDirty()) {
      this.addMusic();
    }
    long posMs = this.te.getPosition();
    long durMs = this.te.getDuration();
    this.slider.setValue(posMs);
    this.slider.maxValue = durMs;
    long posS = posMs / 1000;
    long durS = durMs / 1000;
    this.slider.setMessage(new StringTextComponent(String.format("%02d:%02d/%02d:%02d", posS / 60, posS % 60, durS / 60, durS % 60)));
    if (this.te.isPaused()) {
      this.playButton.setMessage(PLAY_TEXT);
      this.playButton.setIcon(0, 0);
    } else {
      this.playButton.setMessage(PAUSE_TEXT);
      this.playButton.setIcon(1, 0);
    }
    JukeboxTE.Mode mode = this.te.getMode();
    if (mode == JukeboxTE.Mode.SEQUENTIAL) {
      this.modeButton.setMessage(SEQUENTIAL_TEXT);
      this.modeButton.setIcon(0, 2);
    } else if (mode == JukeboxTE.Mode.LOOP_ALL) {
      this.modeButton.setMessage(LOOP_ALL_TEXT);
      this.modeButton.setIcon(1, 2);
    } else if (mode == JukeboxTE.Mode.LOOP_SINGLE) {
      this.modeButton.setMessage(LOOP_SINGLE_TEXT);
      this.modeButton.setIcon(2, 2);
    } else if (mode == JukeboxTE.Mode.RANDOM) {
      this.modeButton.setMessage(RANDOM_TEXT);
      this.modeButton.setIcon(3, 2);
    }
  }
}
