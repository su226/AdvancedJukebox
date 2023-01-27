package su226.jukebox.musics;

import java.util.concurrent.CompletableFuture;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import su226.jukebox.Mod;
import su226.jukebox.icons.IIcon;
import su226.jukebox.icons.ItemIcon;

public class RecordMusic extends Music {
  private static final TranslationTextComponent UNKNOWN_TEXT = new TranslationTextComponent("gui." + Mod.ID + ".unknown_mod");
  private ResourceLocation identifier;
  private ITextComponent songName;
  private ITextComponent modName;
  private ITextComponent itemName;
  private String trackId;
  private IIcon icon;
  private long duration = -1;
  private boolean loaded;

  public RecordMusic(MusicDiscItem item) {
    this.setItem(item);
  }

  public RecordMusic() {
    this.songName = Mod.LOADING_TEXT;
    this.modName = Mod.EMPTY_TEXT;
    this.itemName = Mod.EMPTY_TEXT;
    this.icon = ItemIcon.LOADING;
  }

  private void setItem(MusicDiscItem item) {
    this.loaded = true;
    ItemStack stack = item.getDefaultInstance();
    this.identifier = item.getRegistryName();
    this.songName = item.getDisplayName();
    String modId = item.getCreatorModId(stack);
    if (modId != null) {
      this.modName = new StringTextComponent(ModList.get().getModContainerById(modId)
        .map(modContainer -> modContainer.getModInfo().getDisplayName())
        .orElse(StringUtils.capitalize(modId)));
    } else {
      this.modName = UNKNOWN_TEXT;
    }
    this.itemName = item.getDescription();
    this.icon = new ItemIcon(stack);
    this.trackId = Minecraft.getInstance().getSoundManager().getSoundEvent(item.getSound().getLocation()).getSound().getPath().toString();
  }

  @Override
  public String getType() {
    return "record";
  }

  @Override
  public ResourceLocation getIdentifier() {
    return this.identifier;
  }

  @Override
  public long getDuration() {
    return this.duration;
  }

  @Override
  public boolean isSeekable() {
    return false;
  }

  @Override
  public CompoundNBT serialize() {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putString("item", this.identifier.toString());
    nbt.putLong("duration", this.duration);
    return nbt;
  }

  @Override
  public void deserialize(CompoundNBT nbt) {
    this.duration = nbt.getLong("duration");
    this.identifier = new ResourceLocation(nbt.getString("item"));
  }

  @Override
  public ITextComponent getName() {
    return this.songName;
  }

  @Override
  public ITextComponent getArtist() {
    return this.modName;
  }

  @Override
  public ITextComponent getAlbum() {
    return this.itemName;
  }

  @Override
  public IIcon getIcon() {
    return this.icon;
  }

  @Override
  public String getTrackId() {
    return this.trackId;
  }
  
  @Override
  public CompletableFuture<AudioTrack> loadTrack() {
    this.loadDisplayInfo();
    return super.loadTrack();
  }

  @Override
  public CompletableFuture<Void> loadUploadInfo() {
    return this.loadTrack().thenAccept(track -> {
      this.duration = track.getDuration();
    });
  }

  @Override
  public CompletableFuture<Void> loadDisplayInfo() {
    if (!this.loaded) {
      this.setItem((MusicDiscItem)ForgeRegistries.ITEMS.getValue(this.identifier));
    }
    return CompletableFuture.completedFuture(null);
  }
}
