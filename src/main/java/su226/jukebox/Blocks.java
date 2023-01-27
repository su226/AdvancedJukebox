package su226.jukebox;

import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Blocks {
  public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Mod.ID);
  public static final RegistryObject<Block> JUKEBOX = REGISTER.register("jukebox", Jukebox::new);
}
