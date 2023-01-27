package su226.jukebox.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import su226.jukebox.Mod;

public class Packets {
  public static interface Type<T> {
    void encode(T packet, PacketBuffer buf);
    T decode(PacketBuffer buf);
    void handle(T packet, NetworkEvent.Context ctx);
  }

  private static int id;

  private static <T> void register(Class<T> clazz, Type<T> type) {
    Mod.NETWORK.registerMessage(id++, clazz, type::encode, type::decode, (msg, ctxGetter) -> {
      NetworkEvent.Context ctx = ctxGetter.get();
      ctx.enqueueWork(() -> type.handle(msg, ctx));
      ctx.setPacketHandled(true);
    });
  }

  @SuppressWarnings("unchecked")
  public static <T extends TileEntity> T getTileEneity(Class<T> clazz, NetworkEvent.Context ctx, BlockPos pos) {
    ServerPlayerEntity player = ctx.getSender();
    if (player == null) {
      return clientGetTileEntity(clazz, pos);
    }
    if (!player.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
      Mod.LOG.warn("Position {} isn't within a chunk, might received a broken or malicious packet from player {}", pos, player);
      return null;
    }
    TileEntity te = player.level.getBlockEntity(pos);
    if (te == null) {
      Mod.LOG.warn("Player {} tried to interact to position {} with no TileEntity.", player, pos);
      return null;
    }
    if (!clazz.isInstance(te)) {
      Mod.LOG.warn("Player {} tried to interact to position {} with incorrect TileEntity", player, pos);
      return null;
    }
    return (T)te;
  }

  @SuppressWarnings("unchecked")
  public static <T extends TileEntity> T clientGetTileEntity(Class<T> clazz, BlockPos pos) {
    Minecraft mc = Minecraft.getInstance();
    TileEntity te = mc.level.getBlockEntity(pos);
    return clazz.isInstance(te) ? (T)te : null;
  }

  public static boolean isClient(NetworkEvent.Context ctx) {
    return ctx.getDirection().getReceptionSide() == LogicalSide.CLIENT;
  }

  public static void init() {
    register(JukeboxAddPacket.class, new JukeboxAddPacket.Type());
    register(JukeboxClearPacket.class, new JukeboxClearPacket.Type());
    register(JukeboxMovePacket.class, new JukeboxMovePacket.Type());
    register(JukeboxRemovePacket.class, new JukeboxRemovePacket.Type());
    register(JukeboxSeekPacket.class, new JukeboxSeekPacket.Type());
    register(JukeboxSetCurrentPacket.class, new JukeboxSetCurrentPacket.Type());
    register(JukeboxSetModePacket.class, new JukeboxSetModePacket.Type());
    register(JukeboxSyncPacket.class, new JukeboxSyncPacket.Type());
    register(JukeboxSyncRequestPacket.class, new JukeboxSyncRequestPacket.Type());
  }
}
