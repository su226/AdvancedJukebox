package su226.jukebox.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import su226.jukebox.JukeboxTE;

public class JukeboxClearPacket {
  public static class Type implements Packets.Type<JukeboxClearPacket> {
    @Override
    public void encode(JukeboxClearPacket packet, PacketBuffer buf) {
      buf.writeBlockPos(packet.pos);
    }

    @Override
    public JukeboxClearPacket decode(PacketBuffer buf) {
      return new JukeboxClearPacket(buf.readBlockPos());
    }

    @Override
    public void handle(JukeboxClearPacket packet, Context ctx) {
      JukeboxTE te = Packets.getTileEneity(JukeboxTE.class, ctx, packet.pos);
      if (te != null) {
        te.handleClear(ctx, packet);
      }
    }
  }

  public final BlockPos pos;

  public JukeboxClearPacket(BlockPos pos) {
    this.pos = pos;
  }
}
