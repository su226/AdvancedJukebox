package su226.jukebox.icons;

import java.io.IOException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Util;
import su226.jukebox.Mod;

public class HttpIcon implements IIcon {
  private static enum State {
    PENDING,
    LOADING,
    SUCCESS,
    FAIL,
  }

  private final String url;
  private State state;
  private @Nullable DynamicTexture tex;

  public HttpIcon(String url) {
    this.url = url;
    this.state = State.PENDING;
  }

  public String getUrl() {
    return this.url;
  }

  @Override
  public void render(MatrixStack mat, int x, int y, int size) {
    if (this.state == State.PENDING) {
      this.state = State.LOADING;
      Util.backgroundExecutor().execute(() -> {
        NativeImage image;
        try (CloseableHttpClient http = HttpClients.createDefault()) {
          image = http.execute(new HttpGet(this.url), res -> NativeImage.read(res.getEntity().getContent()));
        } catch (IOException e) {
          this.state = State.FAIL;
          Mod.LOG.error("Failed to load icon: {}", this.url, e);
          return;
        }
        this.tex = new DynamicTexture(image);
        this.state = State.SUCCESS;
      });
    }
    if (this.state == State.LOADING) {
      ItemIcon.LOADING.render(mat, x, y, size);
    } else if (this.state == State.FAIL) {
      ItemIcon.FAIL.render(mat, x, y, size);
    } else {
      GL11.glColor3f(1, 1, 1);
      this.tex.bind();
      Tessellator tess = Tessellator.getInstance();
      BufferBuilder buf = tess.getBuilder();
      buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
      buf.vertex(x, y + size, 0).uv(0, 1).endVertex();
      buf.vertex(x + size, y + size, 0).uv(1, 1).endVertex();
      buf.vertex(x + size, y, 0).uv(1, 0).endVertex();
      buf.vertex(x, y, 0).uv(0, 0).endVertex();
      tess.end();
    }
  }
}
