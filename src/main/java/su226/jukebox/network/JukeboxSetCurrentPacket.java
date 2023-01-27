package su226.jukebox.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import su226.jukebox.JukeboxTE;

public class JukeboxSetCurrentPacket {
  public static class Type implements Packets.Type<JukeboxSetCurrentPacket> {
    @Override
    public void encode(JukeboxSetCurrentPacket packet, PacketBuffer buf) {
      buf.writeBlockPos(packet.pos);
      buf.writeUtf(packet.id == null ? "" : packet.id.toString());
      buf.writeInt(packet.hash);
    }

    @Override
    public JukeboxSetCurrentPacket decode(PacketBuffer buf) {
      BlockPos pos = buf.readBlockPos();
      String id = buf.readUtf();
      return new JukeboxSetCurrentPacket(pos, id.isEmpty() ? null : new ResourceLocation(id), buf.readInt());
    }

    @Override
    public void handle(JukeboxSetCurrentPacket packet, Context ctx) {
      JukeboxTE te = Packets.getTileEneity(JukeboxTE.class, ctx, packet.pos);
      if (te != null) {
        te.handleSetCurrent(ctx, packet);
      }
    }
  }

  public final BlockPos pos;
  public final ResourceLocation id;
  public final int hash;

  public JukeboxSetCurrentPacket(BlockPos pos, ResourceLocation id, int hash) {
    this.pos = pos;
    this.id = id;
    this.hash = hash;
  }
}
