package su226.jukebox.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import su226.jukebox.JukeboxTE;

public class JukeboxSeekPacket {
  public static class Type implements Packets.Type<JukeboxSeekPacket> {
    @Override
    public void encode(JukeboxSeekPacket packet, PacketBuffer buf) {
      buf.writeBlockPos(packet.pos);
      buf.writeLong(packet.musicPos);
      buf.writeBoolean(packet.paused);
      buf.writeInt(packet.hash);
    }

    @Override
    public JukeboxSeekPacket decode(PacketBuffer buf) {
      return new JukeboxSeekPacket(buf.readBlockPos(), buf.readLong(), buf.readBoolean(), buf.readInt());
    }

    @Override
    public void handle(JukeboxSeekPacket packet, Context ctx) {
      JukeboxTE te = Packets.getTileEneity(JukeboxTE.class, ctx, packet.pos);
      if (te != null) {
        te.handleSeek(ctx, packet);
      }
    }
  }

  public final BlockPos pos;
  public final long musicPos;
  public final boolean paused;
  public final int hash;

  public JukeboxSeekPacket(BlockPos pos, long musicPos, boolean paused, int hash) {
    this.pos = pos;
    this.musicPos = musicPos;
    this.paused = paused;
    this.hash = hash;
  }
}
