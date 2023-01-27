package su226.jukebox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import javax.sound.sampled.AudioFormat;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.transcoder.AudioChunkDecoder;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.IAudioStream;

public class LavaPlayerAudioStream implements IAudioStream {
  private static final AudioFormat PLACEHOLDER_FORMAT = new AudioFormat(48000, 16, 1, true, false);
  private final AudioPlayer player;
  private final BooleanSupplier predicate;
  private final Runnable onUnexpectedStop;
  private AudioChunkDecoder decoder;
  private AudioFormat format = PLACEHOLDER_FORMAT;
  private ShortBuffer buffer;
  private int channels;

  public LavaPlayerAudioStream(AudioPlayer player, BooleanSupplier predicate, Runnable onUnexpectedStop) {
    this.player = player;
    this.predicate = predicate;
    this.onUnexpectedStop = onUnexpectedStop;
  }

  private boolean fillBuffer() {
    if (!this.predicate.getAsBoolean()) {
      return false;
    }
    AudioFrame frame = null;
    try {
      frame = this.player.provide(Config.FRAME_TIMEOUT.get(), TimeUnit.MILLISECONDS);
    } catch (TimeoutException | InterruptedException e) {}
    if (frame == null) {
      return false;
    }
    if (this.decoder == null) {
      AudioDataFormat format = frame.getFormat();
      this.decoder = format.createDecoder();
      this.format = new AudioFormat(format.sampleRate, 16, 1, true, false);
      this.buffer = ByteBuffer.allocateDirect(Short.BYTES * format.totalSampleCount()).order(ByteOrder.nativeOrder()).asShortBuffer();
      this.channels = format.channelCount;
    }
    this.buffer.clear();
    this.decoder.decode(frame.getData(), this.buffer);
    return true;
  }

  @Override
  public AudioFormat getFormat() {
    return this.format;
  }

  @Override
  public ByteBuffer read(int maxSize) throws IOException {
    ByteBuffer out = ByteBuffer.allocateDirect(maxSize).order(ByteOrder.LITTLE_ENDIAN);
    while (out.position() < maxSize) {
      if ((this.buffer != null && this.buffer.hasRemaining()) || this.fillBuffer()) {
        int sample = 0;
        for (int i = 0; i < this.channels; i++) {
          sample += this.buffer.get();
        }
        out.putShort((short)(sample / this.channels));
      } else {
        out.putShort((short)0);
      }
    }
    out.rewind();
    return out;
  }

  @Override
  public void close() throws IOException {
    if (this.decoder != null) {
      this.decoder.close();
    }
    Minecraft mc = Minecraft.getInstance();
    if (Mod.hasWorld && mc.gameMode != null && this.predicate.getAsBoolean()) {
      mc.execute(this.onUnexpectedStop);
    }
  }
}
