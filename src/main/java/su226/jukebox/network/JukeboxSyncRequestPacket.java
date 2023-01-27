package su226.jukebox.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import su226.jukebox.JukeboxTE;

public class JukeboxSyncRequestPacket {
  public static class Type implements Packets.Type<JukeboxSyncRequestPacket> {
    @Override
    public void encode(JukeboxSyncRequestPacket packet, PacketBuffer buf) {
      buf.writeBlockPos(packet.pos);
    }

    @Override
    public JukeboxSyncRequestPacket decode(PacketBuffer buf) {
      return new JukeboxSyncRequestPacket(buf.readBlockPos());
    }

    @Override
    public void handle(JukeboxSyncRequestPacket packet, Context ctx) {
      JukeboxTE te = Packets.getTileEneity(JukeboxTE.class, ctx, packet.pos);
      if (te != null) {
        te.sendSyncPacket(ctx);
      }
    }
  }

  public final BlockPos pos;

  public JukeboxSyncRequestPacket(BlockPos pos) {
    this.pos = pos;
  }
}
