package net.runelite.client.plugins.microbot.VaultLooter;

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
        name = PluginDescriptor.Default + "VaultLooter",
        description = "Microbot VaultLooter plugin",
        tags = {"VaultLooter", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class VaultLooterPlugin extends Plugin {
    @Inject
    private VaultLooterConfig config;
    @Provides
    VaultLooterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(VaultLooterConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private VaultLooterOverlay vaultLooterOverlay;

    @Inject
    private VaultLooterScript vaultLooterScript;


    @Override
    protected void startUp() {
        if (overlayManager != null) {
            overlayManager.add(vaultLooterOverlay);
            vaultLooterOverlay.myButton.hookMouseListener();
        }
        vaultLooterScript.run(config);
    }

    protected void shutDown() {
        vaultLooterScript.shutdown();
        overlayManager.remove(vaultLooterOverlay);
        vaultLooterOverlay.myButton.unhookMouseListener();
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
