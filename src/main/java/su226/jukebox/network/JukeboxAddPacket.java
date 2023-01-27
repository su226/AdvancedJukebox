package su226.jukebox.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import su226.jukebox.JukeboxTE;
import su226.jukebox.musics.Music;
import su226.jukebox.musics.Musics;

public class JukeboxAddPacket {
  public static class Type implements Packets.Type<JukeboxAddPacket> {
    @Override
    public void encode(JukeboxAddPacket packet, PacketBuffer buf) {
      buf.writeBlockPos(packet.pos);
      buf.writeNbt(Musics.serialize(packet.music));
      buf.writeInt(packet.hash);
    }

    @Override
    public JukeboxAddPacket decode(PacketBuffer buf) {
      return new JukeboxAddPacket(buf.readBlockPos(), Musics.deserialize(buf.readNbt()), buf.readInt());
    }

    @Override
    public void handle(JukeboxAddPacket packet, Context ctx) {
      JukeboxTE te = Packets.getTileEneity(JukeboxTE.class, ctx, packet.pos);
      if (te != null) {
        te.handleAdd(ctx, packet);
      }
    }
  }

  public final BlockPos pos;
  public final Music music;
  public final int hash;

  public JukeboxAddPacket(BlockPos pos, Music music, int hash) {
    this.pos = pos;
    this.music = music;
    this.hash = hash;
  }
}
