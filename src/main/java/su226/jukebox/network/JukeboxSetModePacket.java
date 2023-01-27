package su226.jukebox.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import su226.jukebox.JukeboxTE;

public class JukeboxSetModePacket {
  public static class Type implements Packets.Type<JukeboxSetModePacket> {
    @Override
    public void encode(JukeboxSetModePacket packet, PacketBuffer buf) {
      buf.writeBlockPos(packet.pos);
      buf.writeInt(packet.mode.ordinal());
      buf.writeInt(packet.hash);
    }

    @Override
    public JukeboxSetModePacket decode(PacketBuffer buf) {
      return new JukeboxSetModePacket(buf.readBlockPos(), JukeboxTE.Mode.values()[buf.readInt()], buf.readInt());
    }

    @Override
    public void handle(JukeboxSetModePacket packet, Context ctx) {
      JukeboxTE te = Packets.getTileEneity(JukeboxTE.class, ctx, packet.pos);
      if (te != null) {
        te.handleSetMode(ctx, packet);
      }
    }
  }

  public final BlockPos pos;
  public final JukeboxTE.Mode mode;
  public final int hash;

  public JukeboxSetModePacket(BlockPos pos, JukeboxTE.Mode mode, int hash) {
    this.pos = pos;
    this.mode = mode;
    this.hash = hash;
  }
}
