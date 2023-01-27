package su226.jukebox;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;

public class MinecraftAudioSourceManager extends ProbingAudioSourceManager {
  private class MinecraftAudioTrack extends DelegatedAudioTrack {
    private final MediaContainerDescriptor factory;
      
    public MinecraftAudioTrack(AudioTrackInfo trackInfo, MediaContainerDescriptor factory) {
      super(trackInfo);
      this.factory = factory;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
      try (IResource res = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(this.trackInfo.identifier))) {
        try (InputStream stream = res.getInputStream()) {
          this.processDelegate((InternalAudioTrack)this.factory.createTrack(this.trackInfo, new NonSeekableInputStream(stream)), executor);
        }
      }
    }
  }

  public MinecraftAudioSourceManager(MediaContainerRegistry containerRegistry) {
    super(containerRegistry);
  }

  public MinecraftAudioSourceManager() {
    this(MediaContainerRegistry.DEFAULT_REGISTRY);
  }

  @Override
  public String getSourceName() {
    return "record";
  }

  @Override
  public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
    Minecraft mc = Minecraft.getInstance();
    ResourceLocation id = new ResourceLocation(reference.identifier);
    int dotIndex = id.getPath().lastIndexOf('.');
    String extension = dotIndex >= 0 ? id.getPath().substring(dotIndex + 1) : null;
    MediaContainerDetectionResult detection;
    try (IResource res = mc.getResourceManager().getResource(id)) {
      detection = new MediaContainerDetection(this.containerRegistry, reference, new NonSeekableInputStream(res.getInputStream()), MediaContainerHints.from(null, extension)).detectContainer();
    } catch (IOException e) {
      throw new FriendlyException("Failed to open Minecraft resource.", Severity.SUSPICIOUS, e);
    }
    return this.handleLoadResult(detection);
  }

  @Override
  public boolean isTrackEncodable(AudioTrack track) {
    return false;
  }

  @Override
  public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {}

  @Override
  public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
    return null;
  }

  @Override
  public void shutdown() {}

  @Override
  protected AudioTrack createTrack(AudioTrackInfo info, MediaContainerDescriptor factory) {
    return new MinecraftAudioTrack(this.getInfoWithDuration(info), factory);
  }
  
  private AudioTrackInfo getInfoWithDuration(AudioTrackInfo info) {
    if (info.length != Long.MAX_VALUE) {
      Mod.LOG.info("Info already contains length: {}", info.length);
      return info;
    }
    ResourceLocation id = new ResourceLocation(info.identifier);
    try (IResource res = Minecraft.getInstance().getResourceManager().getResource(id)) {
      try (InputStream stream = res.getInputStream()) {
        byte[] t = new byte[stream.available()];
        int readBytes = 0;
        while (stream.available() > 0 && readBytes < t.length) {
          readBytes += stream.read(t, readBytes, t.length - readBytes);
        }
        Mod.LOG.debug("Resource {} length {}, read {}", id, t.length, readBytes);
        if (stream.available() > 0) {
          Mod.LOG.warn("Excess {} bytes in resource {}", stream.available(), id);
        } else if (readBytes < t.length) {
          Mod.LOG.warn("Lack of {} bytes in resource {}", t.length - readBytes, id);
        }
        int length = -1;
        for (int i = readBytes - 15; i >= 0 && length < 0; i--) {
          if (t[i] == 'O' && t[i+1] == 'g' && t[i+2] == 'g' && t[i+3] == 'S') {
            length = ByteBuffer.wrap(t, i + 6, 8).order(ByteOrder.LITTLE_ENDIAN).getInt();
            Mod.LOG.debug("Found \"OggS\" at position {}, length: {}", i, length);
          }
        }
        int rate = -1;
        for (int i = 0; i < readBytes - 14 && rate < 0; i++) {
          if (t[i] == 'v' && t[i+1] == 'o' && t[i+2] == 'r' && t[i+3] == 'b' && t[i+4] == 'i' && t[i+5] == 's') {
            rate = ByteBuffer.wrap(t, i + 11, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            Mod.LOG.debug("Found \"vobris\" at position {}, rate: {}", i, rate);
          }
        }
        if (length == -1 || rate == -1) {
          Mod.LOG.error("Unable to calculate OGG Vobris duration, length: {} rate: {}", length, rate);
          throw new FriendlyException("Failed to calculate track duration from Minecraft resource.", Severity.SUSPICIOUS, null);
        }
        return new AudioTrackInfo(info.title, info.author, (length * 1000l) / rate, info.identifier, info.isStream, info.uri);
      }
    } catch (IOException e) {
      throw new FriendlyException("Failed to open Minecraft resource.", Severity.SUSPICIOUS, e);
    }
  }
}
