package su226.jukebox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import su226.jukebox.musics.Music;
import su226.jukebox.musics.Musics;
import su226.jukebox.network.JukeboxAddPacket;
import su226.jukebox.network.JukeboxClearPacket;
import su226.jukebox.network.JukeboxMovePacket;
import su226.jukebox.network.JukeboxRemovePacket;
import su226.jukebox.network.JukeboxSeekPacket;
import su226.jukebox.network.JukeboxSetCurrentPacket;
import su226.jukebox.network.JukeboxSetModePacket;
import su226.jukebox.network.JukeboxSyncPacket;
import su226.jukebox.network.JukeboxSyncRequestPacket;

public class JukeboxTE extends TileEntity implements ITickableTileEntity {
  private static interface ModeFunction {
    int next(int current, int size);
  }
  public static enum Mode {
    SEQUENTIAL((current, size) -> current == size - 1 ? -1 : current + 1),
    LOOP_ALL((current, size) -> current == size - 1 ? 0 : current + 1),
    LOOP_SINGLE((current, size) -> current),
    RANDOM((current, size) -> {
      if (size == 1) {
        return current;
      }
      int next = Mod.RANDOM.nextInt(size - 1);
      return next >= current ? next + 1 : next;
    });

    private final ModeFunction function;
    private Mode(ModeFunction function) {
      this.function = function;
    }
    private int next(int current, int size) {
      return this.function.next(current, size);
    }
  }
  private final Set<ResourceLocation> identifiers;
  private final List<Music> playlist;
  private AudioPlayer player;
  @OnlyIn(Dist.CLIENT)
  private LavaPlayerSound sound;
  private int playId;
  private int loadId;
  private int current = -1;
  private Mode mode = Mode.SEQUENTIAL;
  private boolean paused;
  private long startTick;
  private long currentTick;
  private boolean screenDirty;

  public JukeboxTE() {
    super(TileEntities.JUKEBOX.get());
    this.identifiers = new HashSet<>();
    this.playlist = new ArrayList<>();
  }

  @Override
  public void setLevelAndPosition(World world, BlockPos pos) {
    super.setLevelAndPosition(world, pos);
    if (world.isClientSide && this.player == null) {
      this.player = LavaPlayer.getManager().createPlayer();
    }
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    if (this.level.isClientSide && this.sound != null) {
      this.stopSound();
    }
  }
  
  @Override
  public void onChunkUnloaded() {
    super.onChunkUnloaded();
    if (this.level.isClientSide && this.sound != null) {
      this.stopSound();
    }
  }

  @Override
  public CompoundNBT save(CompoundNBT nbt) {
    super.save(nbt);
    ListNBT playlist = new ListNBT();
    for (Music music : this.playlist) {
      playlist.add(Musics.serialize(music));
    }
    nbt.put("playlist", playlist);
    nbt.putInt("current", this.current);
    nbt.putInt("mode", this.mode.ordinal());
    nbt.putBoolean("paused", this.paused);
    return nbt;
  }

  @Override
  public void load(BlockState state, CompoundNBT nbt) {
    super.load(state, nbt);
    this.playlist.clear();
    this.identifiers.clear();
    for (INBT music : nbt.getList("playlist", 10)) {
      Music m = Musics.deserialize((CompoundNBT)music);
      this.identifiers.add(m.getIdentifier());
      this.playlist.add(m);
    }
    this.current = nbt.getInt("current");
    int mode = nbt.getInt("mode");
    Mode[] modes = Mode.values();
    this.mode = mode >= 0 && mode < modes.length  ? modes[mode] : Mode.SEQUENTIAL;
    long position = nbt.getLong("position");
    this.paused = nbt.getBoolean("paused");
    this.startTick = this.paused ? position : this.currentTick - position / 50;
    if (this.level != null && this.level.isClientSide && !this.paused && this.hasActiveTrack()) {
      this.loadTrack();
    }
  }

  @Override
  public CompoundNBT getUpdateTag() {
    CompoundNBT nbt = this.save(new CompoundNBT());
    nbt.putLong("position", this.getPosition());
    return nbt;
  }

  public void sendSyncPacket(NetworkEvent.Context ctx) {
    Mod.NETWORK.send(PacketDistributor.PLAYER.with(ctx::getSender), new JukeboxSyncPacket(this.worldPosition, this.playlist, this.mode, this.current, this.paused, this.getPosition()));
  }

  public void handleSyncPacket(JukeboxSyncPacket packet) {
    this.playlist.clear();
    this.identifiers.clear();
    for (Music music : packet.playlist) {
      this.identifiers.add(music.getIdentifier());
      this.playlist.add(music);
    }
    this.mode = packet.mode;
    this.screenDirty = true;
    this.current = packet.current;
    this.paused = packet.paused;
    this.startTick = packet.paused ? packet.position : this.currentTick - (packet.position / 50);
    if (this.sound != null) {
      this.stopSound();
    }
    if (!packet.paused) {
      this.loadTrack();
    }
  }
  
  private PacketDistributor.PacketTarget getTracking() {
    return PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunkAt(this.worldPosition));
  }

  private void checkHash(int hash) {
    if (this.level.isClientSide && this.playlist.hashCode() != hash) {
      Mod.LOG.warn("Playlist has incorrect hash, syncing. World: {}, Block: {}", this.level, this.worldPosition);
      this.requestSync();
    }
  }

  public void requestSync() {
    Mod.NETWORK.sendToServer(new JukeboxSyncRequestPacket(this.worldPosition));
  }

  public boolean clearScreenDirty() {
    if (this.screenDirty) {
      this.screenDirty = false;
      return true;
    }
    return false;
  }

  public boolean contains(Music music) {
    return this.identifiers.contains(music.getIdentifier());
  }

  public void requestAdd(Music music) {
    ResourceLocation id = music.getIdentifier();
    if (!this.identifiers.contains(id)) {
      Mod.NETWORK.sendToServer(new JukeboxAddPacket(this.worldPosition, music, -1));
    }
  }

  public void handleAdd(NetworkEvent.Context ctx, JukeboxAddPacket packet) {
    ResourceLocation id = packet.music.getIdentifier();
    if (!this.identifiers.contains(id)) {
      this.identifiers.add(id);
      this.playlist.add(packet.music);
      this.screenDirty = true;
      this.setChanged();
      if (!this.level.isClientSide) {
        Mod.NETWORK.send(this.getTracking(), new JukeboxAddPacket(this.worldPosition, packet.music, this.playlist.hashCode()));
      }
    } else if (!this.level.isClientSide) {
      this.sendSyncPacket(ctx);
    }
    this.checkHash(packet.hash);
  }

  public void requestRemove(ResourceLocation id) {
    if (this.identifiers.contains(id)) {
      Mod.NETWORK.sendToServer(new JukeboxRemovePacket(this.worldPosition, id, -1));
    }
  }

  public void handleRemove(NetworkEvent.Context ctx, JukeboxRemovePacket packet) {
    if (this.identifiers.contains(packet.id)) {
      int index = this.find(packet.id);
      if (this.current == index) {
        this.doSetCurrent(-1);
      } else if (this.current > index) {
        this.current--;
      }
      this.playlist.remove(index);
      this.identifiers.remove(packet.id);
      this.screenDirty = true;
      this.setChanged();
      if (!this.level.isClientSide) {
        Mod.NETWORK.send(this.getTracking(), new JukeboxRemovePacket(this.worldPosition, packet.id, this.playlist.hashCode()));
      }
    } else if (!this.level.isClientSide) {
      this.sendSyncPacket(ctx);
    }
    this.checkHash(packet.hash);
  }

  public void requestClear() {
    if (this.playlist.size() > 0) {
      Mod.NETWORK.sendToServer(new JukeboxClearPacket(this.worldPosition));
    }
  }

  public void handleClear(NetworkEvent.Context ctx, JukeboxClearPacket packet) {
    if (this.playlist.size() > 0) {
      this.doSetCurrent(-1);
      this.playlist.clear();
      this.identifiers.clear();
      this.screenDirty = true;
      this.setChanged();
      if (!this.level.isClientSide) {
        Mod.NETWORK.send(this.getTracking(), new JukeboxClearPacket(this.worldPosition));
      }
    } else if (!this.level.isClientSide) {
      this.sendSyncPacket(ctx);
    }
  }

  public void requestMove(ResourceLocation id, boolean moveUp) {
    if (this.identifiers.contains(id)) {
      int index = this.find(id);
      if (moveUp) {
        if (index > 0 && index < this.playlist.size()) {
          Mod.NETWORK.sendToServer(new JukeboxMovePacket(this.worldPosition, id, true, -1));
        }
      } else if (index >= 0 && index < this.playlist.size() - 1) {
        Mod.NETWORK.sendToServer(new JukeboxMovePacket(this.worldPosition, id, false, -1));
      }
    }
  }

  public void handleMove(NetworkEvent.Context ctx, JukeboxMovePacket packet) {
    if (this.identifiers.contains(packet.id)) {
      int index = this.find(packet.id);
      if (packet.moveUp) {
        this.doMove(ctx, packet, index, index - 1);
      } else {
        this.doMove(ctx, packet, index, index + 1);
      }
    } else if (!this.level.isClientSide) {
      this.sendSyncPacket(ctx);
    }
    this.checkHash(packet.hash);
  }

  private void doMove(NetworkEvent.Context ctx, JukeboxMovePacket packet, int i, int j) {
    if (j < 0 || j >= this.playlist.size()) {
      if (!this.level.isClientSide) {
        this.sendSyncPacket(ctx);
      }
      return;
    }
    Collections.swap(this.playlist, i, j);
    if (this.current == i) {
      this.current = j;
    } else if (this.current == j) {
      this.current = i;
    }
    this.screenDirty = true;
    this.setChanged();
    if (!this.level.isClientSide) {
      Mod.NETWORK.send(this.getTracking(), new JukeboxMovePacket(this.worldPosition, packet.id, packet.moveUp, this.playlist.hashCode()));
    }
  }

  public int find(ResourceLocation id) {
    for (int i = 0; i < this.playlist.size(); i++) {
      if (this.playlist.get(i).getIdentifier().equals(id)) {
        return i;
      }
    }
    return -1;
  }

  public List<Music> getAll() {
    return Collections.unmodifiableList(this.playlist);
  }

  public boolean hasActiveTrack() {
    return this.current >= 0 && this.current < this.playlist.size();
  }

  public ResourceLocation getCurrent() {
    return this.hasActiveTrack() ? this.playlist.get(this.current).getIdentifier() : null;
  }

  public void requestSetCurrent(ResourceLocation id) {
    if (id == null || this.identifiers.contains(id)) {
      Mod.NETWORK.sendToServer(new JukeboxSetCurrentPacket(this.worldPosition, id, -1));
    }
  }

  public void requestPrev() {
    if (this.playlist.size() == 0) {
      this.requestSetCurrent(null);
    } else {
      int prev;
      if (this.mode == Mode.RANDOM) {
        prev = this.mode.next(this.current, this.playlist.size());
      } else {
        prev = this.current <= 0 ? this.playlist.size() - 1 : this.current - 1;
      }
      this.requestSetCurrent(this.playlist.get(prev).getIdentifier());
    }
  }

  public void requestNext() {
    if (this.playlist.size() == 0) {
      this.requestSetCurrent(null);
    } else {
      int next;
      if (this.mode == Mode.RANDOM) {
        next = this.mode.next(this.current, this.playlist.size());
      } else {
        next = this.current == this.playlist.size() - 1 ? 0 : this.current + 1;
      }
      this.requestSetCurrent(this.playlist.get(next).getIdentifier());
    }
  }

  public void handleSetCurrent(NetworkEvent.Context ctx, JukeboxSetCurrentPacket packet) {
    if (packet.id == null || this.identifiers.contains(packet.id)) {
      this.doSetCurrent(packet.id == null ? -1 : this.find(packet.id));
      if (!this.level.isClientSide) {
        Mod.NETWORK.send(this.getTracking(), new JukeboxSetCurrentPacket(this.worldPosition, packet.id, this.playlist.hashCode()));
      }
    } else if (!this.level.isClientSide) {
      this.sendSyncPacket(ctx);
    }
    this.checkHash(packet.hash);
  }

  private void doSetCurrent(int current) {
    if (this.level.isClientSide && this.sound != null) {
      this.stopSound();
      this.player.stopTrack();
    }
    this.current = current;
    this.paused = false;
    this.startTick = this.currentTick;
    this.setChanged();
    if (this.level.isClientSide && this.hasActiveTrack()) {
      this.loadTrack();
      this.showPlaying();
    }
  }

  private void loadTrack() {
    int loadId = Mod.RANDOM.nextInt();
    this.loadId = loadId;
    Mod.loadDelayFuture.thenCompose(unused -> {
      return this.playlist.get(current).loadTrack();
    }).whenComplete((track, e) -> {
      if (e != null) {
        Mod.LOG.error("Failed to load track", e);
        return;
      }
      if (loadId != this.loadId) {
        return;
      }
      this.player.playTrack(track);
      this.player.setPaused(this.paused);
      long position = this.getPosition();
      if (position > Config.EXTRA_MS.get()) {
        track.setPosition(position);
      }
      if (!this.paused) {
        Minecraft.getInstance().execute(this::playSound);
      }
    });
  }

  private void showPlaying() {
    Music music = this.playlist.get(current);
    int loadId = this.loadId;
    Minecraft mc = Minecraft.getInstance();
    music.loadDisplayInfo().thenAcceptAsync(unused -> {
      if (loadId != this.loadId) {
        return;
      }
      int dis = Config.HUD_RADIUS.get();
      if (mc.player.blockPosition().distSqr(this.worldPosition) < dis * dis) {
        mc.gui.setNowPlaying(music.getDisplayName());
      }
    }, mc);
  }

  @OnlyIn(Dist.CLIENT)
  private void playSound() {
    if (this.sound == null) {
      Minecraft mc = Minecraft.getInstance();
      BlockPos pos = this.worldPosition;
      int playId = Mod.RANDOM.nextInt();
      this.playId = playId;
      this.sound = new LavaPlayerSound(player, () -> this.playId == playId, () -> {
        Mod.LOG.warn("LavaPlayerSound accidentally stopped! replaying...");
        this.sound = null;
        this.playSound();
      }, pos.getX(), pos.getY(), pos.getZ());
      mc.getSoundManager().play(this.sound);
    } else {
      Mod.LOG.warn("Already playing LavaPlayerSound", new Exception("Traceback"));
    }
  }

  @OnlyIn(Dist.CLIENT)
  private void stopSound() {
    if (this.sound != null) {
      this.playId = 0;
      Minecraft.getInstance().getSoundManager().stop(this.sound);
      this.sound = null;
    } else {
      Mod.LOG.warn("Not playing LavaPlayerSound", new Exception("Traceback"));
    }
  }

  public Mode getMode() {
    return this.mode;
  }

  public void requestSetMode(Mode mode) {
    Mod.NETWORK.sendToServer(new JukeboxSetModePacket(this.worldPosition, mode, -1));
  }

  public void handleSetMode(NetworkEvent.Context ctx, JukeboxSetModePacket packet) {
    if (this.mode != packet.mode) {
      this.mode = packet.mode;
      this.setChanged();
      if (!this.level.isClientSide) {
        Mod.NETWORK.send(this.getTracking(), new JukeboxSetModePacket(this.worldPosition, packet.mode, this.playlist.hashCode()));
      }
    }
    this.checkHash(packet.hash);
  }

  public boolean isPaused() {
    return this.paused;
  }

  public void requestSetPaused(boolean paused) {
    Mod.NETWORK.sendToServer(new JukeboxSeekPacket(this.worldPosition, this.getPosition(), paused, -1));
  }

  public long getPosition() {
    if (!this.hasActiveTrack()) {
      return 0;
    }
    long duration = this.playlist.get(this.current).getDuration();
    return this.paused ? this.startTick : Math.min((this.currentTick - this.startTick) * 50, duration);
  }

  public long getDuration() {
    if (!this.hasActiveTrack()) {
      return 0;
    }
    return this.playlist.get(this.current).getDuration();
  }

  public void requestSeek(long pos) {
    Mod.NETWORK.sendToServer(new JukeboxSeekPacket(this.worldPosition, pos, this.paused, -1));
  }

  public void handleSeek(NetworkEvent.Context ctx, JukeboxSeekPacket packet) {
    if (!this.hasActiveTrack() || this.playlist.get(this.current).isSeekable()) {
      if (this.level.isClientSide) {
        AudioTrack track = this.player.getPlayingTrack();
        if (track != null) {
          if (this.sound != null) {
            this.stopSound();
          }
          this.player.setPaused(packet.paused);
          track.setPosition(packet.musicPos);
          if (!packet.paused) {
            this.playSound();
          }
        }
      } else {
        Mod.NETWORK.send(this.getTracking(), new JukeboxSeekPacket(this.worldPosition, packet.musicPos, packet.paused, this.playlist.hashCode()));
      }
      this.paused = packet.paused;
      this.startTick = packet.paused ? packet.musicPos : this.currentTick - packet.musicPos / 50;
    }
    this.setChanged();
    this.checkHash(packet.hash);
  }

  @Override
  public void tick() {
    if (!this.hasActiveTrack() || this.paused) {
      return;
    }
    this.currentTick++;
    if (
      !this.level.isClientSide &&
      this.currentTick >= this.startTick + (this.playlist.get(this.current).getDuration() + Config.EXTRA_MS.get()) / 50
    ) {
      int next = this.mode.next(this.current, this.playlist.size());
      this.doSetCurrent(next);
      Mod.NETWORK.send(this.getTracking(), new JukeboxSetCurrentPacket(this.worldPosition, next == -1 ? null : this.playlist.get(next).getIdentifier(), this.playlist.hashCode()));
    }
  }
}
