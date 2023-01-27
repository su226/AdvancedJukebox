package su226.jukebox.icons;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IIcon {
  void render(MatrixStack mat, int x, int y, int size);
}
