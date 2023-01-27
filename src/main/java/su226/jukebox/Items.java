package su226.jukebox;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Items {
  public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Mod.ID);
  public static final RegistryObject<Item> JUKEBOX = REGISTER.register("jukebox", () -> new BlockItem(Blocks.JUKEBOX.get(), new Item.Properties().tab(ItemGroup.TAB_DECORATIONS)));
}
