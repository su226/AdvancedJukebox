package su226.jukebox.gui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.matrix.MatrixStack;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Items;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import su226.jukebox.Config;
import su226.jukebox.JukeboxTE;
import su226.jukebox.Mod;
import su226.jukebox.icons.IIcon;
import su226.jukebox.icons.ItemIcon;
import su226.jukebox.musics.NeteaseMusic;

public class JukeboxScreenNetease extends JukeboxScreen {
  @FunctionalInterface
  private static interface SearchCallback {
    void accept(@Nullable List<NeteaseMusic> result, int total, @Nullable Exception err);
  }

  private static final String SEARCH_API = "https://music.163.com/api/search/get/web?type=1&offset=%d&limit=%d&s=%s";
  private static final TranslationTextComponent SEARCH_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".search");
  private static final TranslationTextComponent PREV_PAGE_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".prev_page");
  private static final TranslationTextComponent NEXT_PAGE_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".next_page");
  private static final TranslationTextComponent VIP_ERROR_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".vip_error");
  private static final TranslationTextComponent NO_RESULT_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".no_result");
  private static final TranslationTextComponent RETRY_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".retry");
  private String prevKeyword;
  private int curPage;
  private int pageCount;
  private int songCount;
  private TransparentTextField searchField;
  private MusicList musicList;
  private int searchId;

  public JukeboxScreenNetease(JukeboxTE te) {
    super(te);
  }

  @Override
  protected String getName() {
    return "netease";
  }

  @Override
  protected int getHeaderHeight() {
    return 40;
  }

  @Override
  protected void init() {
    int buttonWidth = this.font.width(SEARCH_TEXT) + 16;
    this.addButton(new TransparentButton(this.width - 48 - buttonWidth, 20, buttonWidth, 20, SEARCH_TEXT, button -> this.newSearch()));
    this.addButton(new TransparentButton(this.width - 48, 20, 20, 20, PREV_PAGE_TEXT, button -> this.prevPage())).setIcon(2, 1);
    this.addButton(new TransparentButton(this.width - 28, 20, 20, 20, NEXT_PAGE_TEXT, button -> this.nextPage())).setIcon(3, 1);
    this.searchField = new TransparentTextField(this.font, 8, 20, this.width - 56 - buttonWidth, 20, SEARCH_TEXT);
    this.searchField.setOnEnter(this::newSearch);
    this.addWidget(this.searchField);
    this.musicList = new MusicList(this.minecraft, this.width, this.height, 40, this.height - 40, 38);
    this.addWidget(this.musicList);
    super.init();
  }

  private void prevPage() {
    if (!this.searchField.getValue().equals(this.prevKeyword)) {
      this.newSearch();
      return;
    }
    if (this.curPage <= 0) {
      return;
    }
    this.curPage--;
    this.search();
  }

  private void nextPage() {
    if (!this.searchField.getValue().equals(this.prevKeyword)) {
      this.newSearch();
      return;
    }
    if (this.curPage >= this.pageCount - 1) {
      return;
    }
    this.curPage++;
    this.search();
  }

  private void newSearch() {
    this.curPage = 0;
    this.pageCount = 0;
    this.songCount = 0;
    this.search();
  }

  private void search() {
    String keyword = this.searchField.getValue();
    this.prevKeyword = keyword;
    this.musicList.children().clear();
    if (keyword.isEmpty()) {
      return;
    }
    {
      IIcon icon = new ItemIcon(Items.CLOCK.getDefaultInstance());
      this.musicList.setScrollAmount(0);
      this.musicList.addSpecial(hover -> Mod.LOADING_TEXT, null, null, hover -> icon, null);
    }
    int curId = Mod.RANDOM.nextInt();
    this.searchId = curId;
    int pageSize = Config.SEARCH_PAGE_SIZE.get();
    Util.backgroundExecutor().execute(() -> {
      JsonParser json = new JsonParser();
      JsonObject result;
      JsonArray details;
      int total;
      try (CloseableHttpClient http = HttpClients.createDefault()) {
        Mod.LOG.debug("Searching Netease Music, keyword: {}, page: {}", keyword, this.curPage);
        result = http.execute(
          new HttpGet(String.format(SEARCH_API, pageSize * this.curPage, pageSize, URLEncoder.encode(keyword, "UTF-8"))),
          res -> json.parse(new InputStreamReader(res.getEntity().getContent())).getAsJsonObject().getAsJsonObject("result"));
        total = result.get("songCount").getAsInt();
        if (total == 0) {        
          Minecraft.getInstance().execute(() -> {
            if (curId != this.searchId) {
              return;
            }
            IIcon icon = new ItemIcon(Items.BARRIER.getDefaultInstance());
            this.musicList.children().clear();
            this.musicList.addSpecial(hover -> NO_RESULT_TEXT, null, null, hover -> icon, null);
          });
          return;
        }
        String ids = Streams.stream(result.getAsJsonArray("songs")).map(x -> Integer.toString(x.getAsJsonObject().get("id").getAsInt())).collect(Collectors.joining(","));
        details = http.execute(
          new HttpGet(String.format(NeteaseMusic.DETAIL_API, ids)),
          res -> json.parse(new InputStreamReader(res.getEntity().getContent())).getAsJsonObject().getAsJsonArray("songs"));
      } catch (IOException e) {
        Mod.LOG.error("Failed to search Netease Music", e);
        Minecraft.getInstance().execute(() -> {
          if (curId != this.searchId) {
            return;
          }
          ITextComponent text = new StringTextComponent(e.toString());
          IIcon icon = new ItemIcon(Items.BARRIER.getDefaultInstance());
          this.musicList.children().clear();
          this.musicList.addSpecial(hover -> Mod.FAILED_TEXT, hover -> RETRY_TEXT, hover -> text, hover -> icon, null);
        });
        return;
      }
      List<NeteaseMusic> musics = new ArrayList<>();
      JsonArray songs = result.getAsJsonArray("songs");
      for (int i = 0; i < songs.size(); i++) {
        JsonObject song = songs.get(i).getAsJsonObject();
        JsonObject detail = details.get(i).getAsJsonObject();
        String artists = Streams.stream(song.getAsJsonArray("artists")).map(x -> x.getAsJsonObject().get("name").getAsString()).collect(Collectors.joining("/"));
        musics.add(new NeteaseMusic(
          song.get("name").getAsString(),
          artists,
          song.getAsJsonObject("album").get("name").getAsString(),
          detail.getAsJsonObject("album").get("picUrl").getAsString(),
          song.get("fee").getAsInt() == 1 ? -1 : song.get("id").getAsInt(),
          song.get("duration").getAsLong()));
      }
      Minecraft.getInstance().execute(() -> {
        if (curId != this.searchId) {
          return;
        }
        this.musicList.children().clear();
        for (NeteaseMusic music : musics) {
          if (music.isVip()) {
            this.musicList.addMusic(music, null, (text, hover) -> hover ? VIP_ERROR_TEXT : text);
          } else {
            this.musicList.addMusic(music, (x, y) -> this.te.requestAdd(music), (text, hover) -> {
              if (!hover) {
                return text;
              }
              return this.te.contains(music) ? ADDED_TEXT : ADD_TEXT;
            });
          }
        }
        this.pageCount = (int)Math.ceil(1.0 * total / pageSize);
        this.songCount = total;
      });
    });
  }

  @Override
  public void render(MatrixStack mat, int mouseX, int mouseY, float deltaT) {
    this.renderBackground(mat);
    this.musicList.render(mat, mouseX, mouseY, deltaT);
    super.render(mat, mouseX, mouseY, deltaT);
    this.searchField.render(mat, mouseX, mouseY, deltaT);
    if (this.songCount != 0) {
      ITextComponent text = new TranslationTextComponent("gui." + Mod.ID + ".pages", this.curPage + 1, this.pageCount, this.songCount);
      this.font.drawShadow(mat, text, this.width - 8 - this.font.width(text), 6, 0xffffff);
    }
  }
}
