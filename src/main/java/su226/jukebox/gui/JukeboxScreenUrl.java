package su226.jukebox.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;

import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import su226.jukebox.JukeboxTE;
import su226.jukebox.Mod;
import su226.jukebox.musics.CustomMusic;

public class JukeboxScreenUrl extends JukeboxScreen {
  private static final TranslationTextComponent URL_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".url");
  private static final TranslationTextComponent CUSTOM_NAME_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".custom_name");
  private TransparentTextField nameField;
  private TransparentTextField urlField;
  private ITextComponent message;
  private boolean error;

  public JukeboxScreenUrl(JukeboxTE te) {
    super(te);
  }

  @Override
  protected void init() {
    super.init();
    this.urlField = this.addWidget(new TransparentTextField(this.font, 8, 28, this.width - 16, 20, URL_TEXT));
    this.urlField.setMaxLength(2048);
    this.urlField.setDrawPlaceholder(true);
    this.nameField = this.addWidget(new TransparentTextField(this.font, 8, 52, this.width - 16, 20, CUSTOM_NAME_TEXT));
    this.nameField.setDrawPlaceholder(true);
    int buttonWidth = this.font.width(ADD_TEXT) + 16;
    this.addButton(new TransparentButton(this.width - buttonWidth - 8, 0, buttonWidth, 20, ADD_TEXT, button -> this.addUrl()));
  }

  @Override
  protected String getName() {
    return "url";
  }

  private void addUrl() {
    String url = this.urlField.getValue().trim();
    if (url.isEmpty()) {
      return;
    }
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      url = "http://" + url;
    }
    String name = this.nameField.getValue().trim();
    CustomMusic music = new CustomMusic(name, url);
    this.message = null;
    music.loadUploadInfo().whenComplete((track, e) -> {
      if (e != null) {
        this.error = true;
        this.message = new StringTextComponent(this.getFriendlyException(e));
        return;
      }
      this.te.requestAdd(music);
      this.error = false;
      this.message = ADDED_TEXT;
    });
  }

  private String getFriendlyException(Throwable e) {
    for (Throwable e2 = e; e2 != null; e2 = e2.getCause()) {
      if (e2 instanceof FriendlyException) {
        return e2.getMessage();
      }
    }
    return e.toString();
  }

  @Override
  public void render(MatrixStack mat, int mouseX, int mouseY, float deltaT) {
    this.renderBackground(mat);
    super.render(mat, mouseX, mouseY, deltaT);
    urlField.render(mat, mouseX, mouseY, deltaT);
    nameField.render(mat, mouseX, mouseY, deltaT);
    if (this.message != null) {
      for (IReorderingProcessor line : this.font.split(this.message, this.width - 16)) {
        this.font.drawShadow(mat, line, 8, 76, this.error ? 0xff5555 : 0xffffff);
      }
    }
  }
}
