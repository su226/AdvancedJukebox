package su226.jukebox.musics;

import java.util.concurrent.CompletableFuture;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import su226.jukebox.LavaPlayer;
import su226.jukebox.Mod;
import su226.jukebox.icons.IIcon;

public abstract class Music {
  public abstract String getType();
  public abstract ResourceLocation getIdentifier();
  public abstract long getDuration();
  public abstract boolean isSeekable();
  public abstract CompoundNBT serialize();
  public abstract void deserialize(CompoundNBT nbt);

  public abstract ITextComponent getName();
  public abstract ITextComponent getArtist();
  public abstract ITextComponent getAlbum();
  public abstract IIcon getIcon();
  public abstract String getTrackId();
  public ITextComponent getDisplayName() {
    return this.getName();
  }
  public CompletableFuture<AudioTrack> loadTrack() {
    CompletableFuture<AudioTrack> future = new CompletableFuture<>();
    String trackId = this.getTrackId();
    LavaPlayer.getManager().loadItem(trackId, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        future.complete(track);
      }
      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        Mod.LOG.warn("A playlist loaded, which is not supported, only the first track will be played: {}", playlist);
        future.complete(playlist.getTracks().get(0));
      }
      @Override
      public void noMatches() {
        future.completeExceptionally(new FriendlyException("This URL doesn't seem to be a track.", Severity.COMMON, null));
      }
      @Override
      public void loadFailed(FriendlyException e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }
  public CompletableFuture<Void> loadUploadInfo() {
    return CompletableFuture.completedFuture(null);
  }
  public CompletableFuture<Void> loadDisplayInfo() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public int hashCode() {
    return this.getIdentifier().hashCode();
  }
}
