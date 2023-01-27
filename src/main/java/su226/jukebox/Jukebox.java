package su226.jukebox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import su226.jukebox.gui.JukeboxScreen;

public class Jukebox extends Block {
  public Jukebox() {
    super(Properties.of(Material.WOOD, MaterialColor.DIRT).strength(2.0F, 6.0F));
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new JukeboxTE();
  }

  @Override
  public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
    if (world.isClientSide && hand == Hand.MAIN_HAND) {
      TileEntity te = world.getBlockEntity(pos);
      if (te instanceof JukeboxTE) {
        JukeboxScreen.openFirst((JukeboxTE)te);
      }
    }
    return ActionResultType.SUCCESS;
  }
}
