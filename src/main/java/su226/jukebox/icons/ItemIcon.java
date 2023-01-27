package su226.jukebox.icons;

import com.mojang.blaze3d.matrix.MatrixStack;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemIcon implements IIcon {
  public static final ItemIcon LOADING = new ItemIcon(Items.CLOCK.getDefaultInstance());
  public static final ItemIcon FAIL = new ItemIcon(Items.BARRIER.getDefaultInstance());
  private final ItemStack stack;

  public ItemIcon(ItemStack stack) {
    this.stack = stack;
  }

  @Override
  public void render(MatrixStack mat, int x, int y, int size) {
    Minecraft mc = Minecraft.getInstance();
    int offset = (size - 32) / 2;
    GL11.glPushMatrix();
    GL11.glTranslatef(x + offset, y + offset, 0);
    GL11.glScalef(2, 2, 2);
    mc.getItemRenderer().renderGuiItem(this.stack, 0, 0);
    GL11.glPopMatrix();
  }
}
