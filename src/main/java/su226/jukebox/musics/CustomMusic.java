package su226.jukebox.musics;

import java.util.concurrent.CompletableFuture;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;

import org.apache.commons.codec.digest.DigestUtils;

import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import su226.jukebox.Mod;
import su226.jukebox.icons.IIcon;
import su226.jukebox.icons.ItemIcon;

public class CustomMusic extends Music {
  private static final TranslationTextComponent NO_NAME_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".no_name");
  private static final ItemIcon ICON = new ItemIcon(Items.COBWEB.getDefaultInstance());
  private StringTextComponent name;
  private StringTextComponent url;
  private ResourceLocation id;
  private long duration;
  private boolean seekable;

  public CustomMusic(String name, String url) {
    this.name = name.isEmpty() ? null : new StringTextComponent(name);
    this.setUrl(url);
  }

  public CustomMusic() {}

  private void setUrl(String url) {
    this.url = new StringTextComponent(url);
    this.id = new ResourceLocation(Mod.ID, "http_" + DigestUtils.md5Hex(url));
  }

  @Override
  public String getType() {
    return "custom";
  }

  @Override
  public ResourceLocation getIdentifier() {
    return this.id;
  }

  @Override
  public long getDuration() {
    return this.duration;
  }

  @Override
  public boolean isSeekable() {
    return this.seekable;
  }

  @Override
  public CompoundNBT serialize() {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putString("name", this.name == null ? "" : this.name.getText());
    nbt.putString("url", this.url.getText());
    nbt.putLong("duration", this.duration);
    nbt.putBoolean("seekable", this.seekable);
    return nbt;
  }

  @Override
  public void deserialize(CompoundNBT nbt) {
    String name = nbt.getString("name");
    this.name = name.isEmpty() ? null : new StringTextComponent(name);
    this.setUrl(nbt.getString("url"));
    this.duration = nbt.getLong("duration");
    this.seekable = nbt.getBoolean("seekable");
  }

  @Override
  public ITextComponent getName() {
    return this.name == null ? NO_NAME_TEXT : this.name;
  }

  @Override
  public ITextComponent getArtist() {
    return Mod.EMPTY_TEXT;
  }

  @Override
  public ITextComponent getAlbum() {
    return this.url;
  }

  @Override
  public IIcon getIcon() {
    return ICON;
  }

  @Override
  public String getTrackId() {
    return this.url.getText();
  }

  @Override
  public CompletableFuture<Void> loadUploadInfo() {
    return this.loadTrack().thenAccept(track -> {
      long duration = track.getDuration();
      if (duration == Long.MAX_VALUE) {
        throw new FriendlyException("Cannot get track duration", Severity.COMMON, null);
      }
      this.duration = duration;
      this.seekable = track.isSeekable();
    });
  }
}
