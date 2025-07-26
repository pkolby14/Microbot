package net.runelite.client.plugins.microbot.commandbridge;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = "Command Bridge",
        description = "Universal command execution bridge for multi-client automation",
        tags = {"microbot", "automation", "api", "bridge", "remote"},
        enabledByDefault = false
)
@Slf4j
public class CommandBridgePlugin extends Plugin {

    @Inject
    private CommandBridgeConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private CommandBridgeOverlay commandBridgeOverlay;

    public CommandBridgeScript commandBridgeScript = new CommandBridgeScript();

    @Provides
    CommandBridgeConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CommandBridgeConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        log.info("Command Bridge plugin starting up");

        if (overlayManager != null) {
            overlayManager.add(commandBridgeOverlay);
        }

        // Start the command bridge script
        commandBridgeScript.run(config);

        log.info("Command Bridge plugin started successfully");
    }

    @Override
    protected void shutDown() {
        log.info("Command Bridge plugin shutting down");

        // Shutdown the script
        commandBridgeScript.shutdown();

        // Remove overlay
        if (overlayManager != null) {
            overlayManager.remove(commandBridgeOverlay);
        }

        log.info("Command Bridge plugin shut down successfully");
    }

    /**
     * Get the current script instance
     */
    public CommandBridgeScript getScript() {
        return commandBridgeScript;
    }
}