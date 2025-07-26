package net.runelite.client.plugins.microbot.RevBot;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "RevBot",
        description = "RevBot plugin",
        tags = {"revbot", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class RevBotPlugin extends Plugin {
    @Inject
    private RevBotConfig config;
    @Provides
    RevBotConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(RevBotConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private RevBotOverlay revBotOverlay;

    @Inject
    RevBotScript revBotScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(revBotOverlay);
            revBotOverlay.myButton.hookMouseListener();
        }
        revBotScript.run(config);
    }

    protected void shutDown() {
        revBotScript.shutdown();
        overlayManager.remove(revBotOverlay);
        revBotOverlay.myButton.unhookMouseListener();
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }

}
