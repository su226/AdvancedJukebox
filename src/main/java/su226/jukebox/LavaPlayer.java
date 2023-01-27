package su226.jukebox;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;

public class LavaPlayer {
  private static AudioPlayerManager manager;

  public static AudioPlayerManager getManager() {
    return manager;
  }

  public static void init() {
    manager = new DefaultAudioPlayerManager();
    manager.setUseSeekGhosting(false);
    manager.registerSourceManager(new HttpAudioSourceManager());
    manager.registerSourceManager(new MinecraftAudioSourceManager());
  }
}
