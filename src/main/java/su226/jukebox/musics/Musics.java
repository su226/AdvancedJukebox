package su226.jukebox.musics;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;

public class Musics {
  private static final Map<String, Supplier<Music>> TYPES = new HashMap<>();

  public static Music deserialize(CompoundNBT nbt) {
    Music music = TYPES.get(nbt.getString("type")).get();
    music.deserialize(nbt);
    return music;
  }

  public static CompoundNBT serialize(Music music) {
    CompoundNBT nbt = music.serialize();
    nbt.putString("type", music.getType());
    return nbt;
  }

  public static void init() {
    TYPES.put("record", RecordMusic::new);
    TYPES.put("netease", NeteaseMusic::new);
    TYPES.put("custom", CustomMusic::new);
  }
}
