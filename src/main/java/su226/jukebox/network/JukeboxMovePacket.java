package su226.jukebox.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import su226.jukebox.JukeboxTE;

public class JukeboxMovePacket {
  public static class Type implements Packets.Type<JukeboxMovePacket> {
    @Override
    public void encode(JukeboxMovePacket packet, PacketBuffer buf) {
      buf.writeBlockPos(packet.pos);
      buf.writeUtf(packet.id.toString());
      buf.writeBoolean(packet.moveUp);
      buf.writeInt(packet.hash);
    }

    @Override
    public JukeboxMovePacket decode(PacketBuffer buf) {
      return new JukeboxMovePacket(buf.readBlockPos(), new ResourceLocation(buf.readUtf()), buf.readBoolean(), buf.readInt());
    }

    @Override
    public void handle(JukeboxMovePacket packet, Context ctx) {
      JukeboxTE te = Packets.getTileEneity(JukeboxTE.class, ctx, packet.pos);
      if (te != null) {
        te.handleMove(ctx, packet);
      }
    }
  }

  public final BlockPos pos;
  public final ResourceLocation id;
  public final boolean moveUp;
  public final int hash;

  public JukeboxMovePacket(BlockPos pos, ResourceLocation id, boolean moveUp, int hash) {
    this.pos = pos;
    this.id = id;
    this.moveUp = moveUp;
    this.hash = hash;
  }
}
