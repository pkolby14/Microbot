package net.runelite.client.plugins.microbot.PL1;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import java.awt.AWTException;

@PluginDescriptor(
        name = "PL1",
        description = "PL1 Script",
        tags = {"microbot", "script", "pl1"},
        enabledByDefault = false
)
@Slf4j
@Singleton
public class PL1Plugin extends Plugin {
    @Inject private PL1Config config;
    @Inject private OverlayManager overlayManager;
    @Inject private PL1Overlay overlay;
    @Inject private PL1Script script;

    @Getter private boolean running = false;

    @Provides
    PL1Config provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PL1Config.class);
    }

    @Override
    protected void startUp() throws AWTException {
        log.info("PL1Plugin: Starting plugin...");
        if (script == null) {
            log.error("Failed to initialize script - script is null");
            return;
        }
        running = true;
        overlayManager.add(overlay);
        script.run(config);
    }

    @Override
    protected void shutDown() {
        log.info("PL1Plugin: Shutting down plugin...");
        running = false;
        if (script != null) {
            script.shutdown();
        }
        overlayManager.remove(overlay);
    }
}