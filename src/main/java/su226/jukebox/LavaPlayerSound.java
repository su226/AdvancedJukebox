package su226.jukebox;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.minecraft.client.audio.AudioStreamManager;
import net.minecraft.client.audio.IAudioStream;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class LavaPlayerSound extends LocatableSound {
  public static final ResourceLocation ID = new ResourceLocation(Mod.ID, "lavaplayer");
  public final AudioPlayer player;
  public final BooleanSupplier predicate;
  public final Runnable onUnexpectedStop;

  public LavaPlayerSound(AudioPlayer player, BooleanSupplier predicate, Runnable onUnexpectedStop, double x, double y, double z) {
    super(ID, SoundCategory.RECORDS);
    this.player = player;
    this.predicate = predicate;
    this.onUnexpectedStop = onUnexpectedStop;
    this.volume = Config.VOLUME.get().floatValue();
    this.x = x;
    this.y = y;
    this.z = z;
    this.sound = new Sound(this.location.toString(), 1, 1, 1, Sound.Type.FILE, true, false, 16);
  }

  @Override
  public SoundEventAccessor resolve(SoundHandler handler) {
    SoundEventAccessor accessor = new SoundEventAccessor(ID, null);
    accessor.addSound(this.sound);
    return accessor;
  }

  @Override
  public CompletableFuture<IAudioStream> getStream(AudioStreamManager soundBuffers, Sound sound, boolean looping) {
    return CompletableFuture.completedFuture(new LavaPlayerAudioStream(this.player, this.predicate, this.onUnexpectedStop));
  }
}
