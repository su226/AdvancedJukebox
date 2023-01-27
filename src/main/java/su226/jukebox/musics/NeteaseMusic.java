package su226.jukebox.musics;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import su226.jukebox.Mod;
import su226.jukebox.icons.HttpIcon;
import su226.jukebox.icons.IIcon;
import su226.jukebox.icons.ItemIcon;

public class NeteaseMusic extends Music {
  public static final String DETAIL_API = "https://music.163.com/api/song/detail/?ids=[%s]";
  private static final String SONG_URL = "https://music.163.com/song/media/outer/url?id=%d.mp3";
  private boolean loaded;
  private IFormattableTextComponent name;
  private ITextComponent artist;
  private ITextComponent album;
  private IIcon icon;
  private int songId;
  private ResourceLocation id;
  private String url;
  private long duration;

  public NeteaseMusic(String name, String artist, String album, String coverUrl, int songId, long duration) {
    this.loaded = true;
    this.name = new StringTextComponent(name);
    this.artist = new StringTextComponent(artist);
    this.album = new StringTextComponent(album);
    this.icon = new HttpIcon(coverUrl);
    this.songId = songId;
    if (songId == -1) {
      this.name.setStyle(this.name.getStyle().setStrikethrough(true));
    } else {
      this.id = new ResourceLocation(Mod.ID, String.format("netease_%d", songId));
      this.url = String.format(SONG_URL, songId);
    }
    this.duration = duration;
  }

  public NeteaseMusic() {
    this.name = Mod.LOADING_TEXT;
    this.artist = Mod.EMPTY_TEXT;
    this.album = Mod.EMPTY_TEXT;
    this.icon = ItemIcon.LOADING;
  }

  @Override
  public String getType() {
    return "netease";
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
    return true;
  }

  @Override
  public CompoundNBT serialize() {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putInt("id", this.songId);
    nbt.putLong("duration", this.duration);
    return nbt;
  }

  @Override
  public void deserialize(CompoundNBT nbt) {
    this.songId = nbt.getInt("id");
    this.id = new ResourceLocation(Mod.ID, String.format("netease_%d", this.songId));
    this.url = String.format(SONG_URL, this.songId);
    this.duration = nbt.getLong("duration");
  }

  @Override
  public ITextComponent getName() {
    return this.name;
  }

  @Override
  public ITextComponent getArtist() {
    return this.artist;
  }

  @Override
  public ITextComponent getAlbum() {
    return this.album;
  }

  @Override
  public IIcon getIcon() {
    return this.icon;
  }

  @Override
  public String getTrackId() {
    return this.url;
  }

  @Override
  public ITextComponent getDisplayName() {
    return this.artist.plainCopy().append(" - ").append(this.name);
  }

  @Override
  public CompletableFuture<Void> loadDisplayInfo() {
    if (this.loaded) {
      return CompletableFuture.completedFuture(null);
    }
    return CompletableFuture.runAsync(() -> {
      JsonParser json = new JsonParser();
      JsonObject detail;
      try (CloseableHttpClient http = HttpClients.createDefault()) {
        detail = http.execute(
          new HttpGet(String.format(NeteaseMusic.DETAIL_API, this.songId)),
          res -> json.parse(new InputStreamReader(res.getEntity().getContent())).getAsJsonObject().getAsJsonArray("songs").get(0).getAsJsonObject());
      } catch (IOException e) {
        this.name = Mod.FAILED_TEXT;
        this.icon = ItemIcon.FAIL;
        return;
      }
      this.loaded = true;
      this.name = new StringTextComponent(detail.get("name").getAsString());
      this.artist = new StringTextComponent(Streams.stream(detail.getAsJsonArray("artists")).map(x -> x.getAsJsonObject().get("name").getAsString()).collect(Collectors.joining("/")));
      this.album = new StringTextComponent(detail.getAsJsonObject("album").get("name").getAsString());
      this.icon = new HttpIcon(detail.getAsJsonObject("album").get("picUrl").getAsString());
    }, Util.backgroundExecutor());
  }

  public boolean isVip() {
    return this.url == null;
  }
}
