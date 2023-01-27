package su226.jukebox;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntities {
  public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Mod.ID);
  public static final RegistryObject<TileEntityType<JukeboxTE>> JUKEBOX = REGISTER.register("jukebox", () -> TileEntityType.Builder.of(JukeboxTE::new, Blocks.JUKEBOX.get()).build(null));
}
