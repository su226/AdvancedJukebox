package su226.jukebox;

import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import su226.jukebox.musics.Musics;
import su226.jukebox.network.Packets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@net.minecraftforge.fml.common.Mod(Mod.ID)
public class Mod {
  public static final String ID = "advanced_jukebox";
  public static final String PROTOCOL = "1";
  public static final Logger LOG = LogManager.getLogger(ID);
  public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(ID, "main"), () -> PROTOCOL, NetworkRegistry.acceptMissingOr(PROTOCOL::equals), PROTOCOL::equals);
  public static final Random RANDOM = new Random();
  public static final StringTextComponent EMPTY_TEXT = new StringTextComponent("");
  public static final TranslationTextComponent LOADING_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".loading");
  public static final TranslationTextComponent FAILED_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".failed");
  public static final List<MusicDiscItem> DISC_ITEMS = new ArrayList<>();
  public static CompletableFuture<Void> loadDelayFuture = CompletableFuture.completedFuture(null);
  public static boolean hasWorld;

  public Mod() {
    Config.init();
    Packets.init();
    Musics.init();
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    Blocks.REGISTER.register(bus);
    Items.REGISTER.register(bus);
    TileEntities.REGISTER.register(bus);
    bus.register(this);
  }

  @SubscribeEvent
  public void onClientStartup(FMLClientSetupEvent event) {
    LavaPlayer.init();
    Mod.LOG.info("Scanning records begin.");
    long t = System.currentTimeMillis();
    ForgeRegistries.ITEMS.forEach(item -> {
      if (item instanceof MusicDiscItem) {
        DISC_ITEMS.add((MusicDiscItem)item);
      }
    });
    Mod.LOG.info("Scanning records took {} ms.", System.currentTimeMillis() - t);
  }

  @EventBusSubscriber(Dist.CLIENT)
  public static class ClientEvents {
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
      if (!event.getWorld().isClientSide()) {
        return; // Only logical client, not integrated server.
      }
      hasWorld = true;
      loadDelayFuture = CompletableFuture.runAsync(() -> {
        try {
          Thread.sleep(Config.LOAD_DELAY.get());
        } catch (InterruptedException e) {}
      });
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
      if (event.getWorld().isClientSide()) {
        hasWorld = false;
      }
    }
  }
}
