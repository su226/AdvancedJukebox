package su226.jukebox;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {
  public static final ForgeConfigSpec COMMON_CONFIG;
  public static final ForgeConfigSpec.LongValue EXTRA_MS;

  public static final ForgeConfigSpec CLIENT_CONFIG;
  public static final ForgeConfigSpec.IntValue SEARCH_PAGE_SIZE;
  public static final ForgeConfigSpec.LongValue LOAD_DELAY;
  public static final ForgeConfigSpec.LongValue FRAME_TIMEOUT;
  public static final ForgeConfigSpec.DoubleValue VOLUME;
  public static final ForgeConfigSpec.IntValue HUD_RADIUS;

  static {
    ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    EXTRA_MS = COMMON_BUILDER.comment("Extra milliseconds per song.").translation("config.advanced_jukebox.extra_ms").defineInRange("extra_ms", 1000, 0, Long.MAX_VALUE);
    COMMON_CONFIG = COMMON_BUILDER.build();

    ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    SEARCH_PAGE_SIZE = CLIENT_BUILDER.comment("Page size when searching Internet music.").translation("config.advanced_jukebox.search_page_size").defineInRange("search_page_size", 20, 1, 100);
    LOAD_DELAY = CLIENT_BUILDER.comment("Track load delay when joining world, in milliseconds.").translation("config.advanced_jukebox.load_delay").defineInRange("load_delay", 0, 0, Long.MAX_VALUE);
    FRAME_TIMEOUT = CLIENT_BUILDER.comment("Timeout in milliseconds when loading one frame(20ms audio).").translation("config.advanced_jukebox.frame_timeout").defineInRange("frame_timeout", 20, 0, Long.MAX_VALUE);
    VOLUME = CLIENT_BUILDER.comment("Sound volume, the same in playsound command.").translation("config.advanced_jukebox.volume").defineInRange("volume", 4, 0, Double.MAX_VALUE);
    HUD_RADIUS = CLIENT_BUILDER.comment("Max block distance of \"Now Playing\" HUD tooltip, 0 to disable.").translation("config.advanced_jukebox.hud_radius").defineInRange("hud_radius", 48, 0, Integer.MAX_VALUE);
    CLIENT_CONFIG = CLIENT_BUILDER.build();
  }

  public static void init() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
  }
}
