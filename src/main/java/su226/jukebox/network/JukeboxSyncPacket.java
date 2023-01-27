package su226.jukebox.network;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import su226.jukebox.JukeboxTE;
import su226.jukebox.musics.Music;
import su226.jukebox.musics.Musics;

public class JukeboxSyncPacket {
  public static class Type implements Packets.Type<JukeboxSyncPacket> {
    @Override
    public void encode(JukeboxSyncPacket packet, PacketBuffer buf) {
      buf.writeBlockPos(packet.pos);
      buf.writeInt(packet.playlist.size());
      for (Music music : packet.playlist) {
        buf.writeNbt(Musics.serialize(music));
      }
      buf.writeByte(packet.mode.ordinal());
      buf.writeInt(packet.current);
      buf.writeBoolean(packet.paused);
      buf.writeLong(packet.position);
    }

    @Override
    public JukeboxSyncPacket decode(PacketBuffer buf) {
      BlockPos pos = buf.readBlockPos();
      Music[] musics = new Music[buf.readInt()];
      for (int i = 0; i < musics.length; i++) {
        musics[i] = Musics.deserialize(buf.readNbt());
      }
      JukeboxTE.Mode mode = JukeboxTE.Mode.values()[buf.readByte()];
      int current = buf.readInt();
      boolean paused = buf.readBoolean();
      long position = buf.readLong();
      return new JukeboxSyncPacket(pos, Arrays.asList(musics), mode, current, paused, position);
    }

    @Override
    public void handle(JukeboxSyncPacket packet, Context ctx) {
      Minecraft mc = Minecraft.getInstance();
      TileEntity te = mc.level.getBlockEntity(packet.pos);
      if (te instanceof JukeboxTE) {
        ((JukeboxTE)te).handleSyncPacket(packet);
      }
    }
  }

  public final BlockPos pos;
  public final List<Music> playlist;
  public final JukeboxTE.Mode mode;
  public final int current;
  public final boolean paused;
  public final long position;

  public JukeboxSyncPacket(BlockPos pos, List<Music> playlist, JukeboxTE.Mode mode, int current, boolean paused, long position) {
    this.pos = pos;
    this.playlist = playlist;
    this.mode = mode;
    this.current = current;
    this.paused = paused;
    this.position = position;
  }
}
