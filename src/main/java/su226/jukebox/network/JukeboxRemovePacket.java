package su226.jukebox.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import su226.jukebox.JukeboxTE;

public class JukeboxRemovePacket {
  public static class Type implements Packets.Type<JukeboxRemovePacket> {
    @Override
    public void encode(JukeboxRemovePacket packet, PacketBuffer buf) {
      buf.writeBlockPos(packet.pos);
      buf.writeUtf(packet.id.toString());
      buf.writeInt(packet.hash);
    }

    @Override
    public JukeboxRemovePacket decode(PacketBuffer buf) {
      return new JukeboxRemovePacket(buf.readBlockPos(), new ResourceLocation(buf.readUtf()), buf.readInt());
    }

    @Override
    public void handle(JukeboxRemovePacket packet, Context ctx) {
      JukeboxTE te = Packets.getTileEneity(JukeboxTE.class, ctx, packet.pos);
      if (te != null) {
        te.handleRemove(ctx, packet);
      }
    }
  }

  public final BlockPos pos;
  public final ResourceLocation id;
  public final int hash;

  public JukeboxRemovePacket(BlockPos pos, ResourceLocation id, int hash) {
    this.pos = pos;
    this.id = id;
    this.hash = hash;
  }
}
