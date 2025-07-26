# Now let's create the enhanced CommandBridgePlugin.java
command_bridge_plugin = '''package net.runelite.client.plugins.microbot.commandbridge;

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
}'''

# Create the enhanced CommandBridgeConfig.java
command_bridge_config = '''package net.runelite.client.plugins.microbot.commandbridge;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("commandbridge")
public interface CommandBridgeConfig extends Config {

    @ConfigSection(
        name = "Connection Settings",
        description = "Server connection configuration",
        position = 0
    )
    String connectionSection = "connection";

    @ConfigItem(
        keyName = "serverUrl",
        name = "Server URL",
        description = "The URL of your command server (e.g., http://localhost:8080)",
        position = 1,
        section = connectionSection
    )
    default String serverUrl() {
        return "http://localhost:8080";
    }

    @ConfigItem(
        keyName = "clientId",
        name = "Client ID",
        description = "Unique identifier for this client",
        position = 2,
        section = connectionSection
    )
    default String clientId() {
        return "bot_1";
    }

    @ConfigItem(
        keyName = "enableLogging",
        name = "Enable Logging",
        description = "Enable detailed logging for debugging",
        position = 3,
        section = connectionSection
    )
    default boolean enableLogging() {
        return true;
    }

    @ConfigSection(
        name = "Command Settings",
        description = "Command execution configuration",
        position = 1
    )
    String commandSection = "commands";

    @ConfigItem(
        keyName = "commandTimeout",
        name = "Command Timeout (ms)",
        description = "Default timeout for command execution",
        position = 1,
        section = commandSection
    )
    default int commandTimeout() {
        return 30000;
    }

    @ConfigItem(
        keyName = "pollInterval",
        name = "Poll Interval (ms)",
        description = "How often to check for new commands",
        position = 2,
        section = commandSection
    )
    default int pollInterval() {
        return 1000;
    }

    @ConfigItem(
        keyName = "maxQueueSize",
        name = "Max Queue Size",
        description = "Maximum number of queued commands",
        position = 3,
        section = commandSection
    )
    default int maxQueueSize() {
        return 10;
    }

    @ConfigSection(
        name = "Safety Settings",
        description = "Safety and anti-ban configuration",
        position = 2
    )
    String safetySection = "safety";

    @ConfigItem(
        keyName = "enableSafetyChecks",
        name = "Enable Safety Checks",
        description = "Enable basic safety checks before executing commands",
        position = 1,
        section = safetySection
    )
    default boolean enableSafetyChecks() {
        return true;
    }

    @ConfigItem(
        keyName = "minActionDelay",
        name = "Min Action Delay (ms)",
        description = "Minimum delay between actions for anti-ban",
        position = 2,
        section = safetySection
    )
    default int minActionDelay() {
        return 600;
    }

    @ConfigItem(
        keyName = "maxActionDelay",
        name = "Max Action Delay (ms)",
        description = "Maximum delay between actions for anti-ban",
        position = 3,
        section = safetySection
    )
    default int maxActionDelay() {
        return 1200;
    }
}'''

# Create the enhanced CommandBridgeOverlay.java
command_bridge_overlay = '''package net.runelite.client.plugins.microbot.commandbridge;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class CommandBridgeOverlay extends OverlayPanel {

    private final CommandBridgePlugin plugin;

    @Inject
    public CommandBridgeOverlay(CommandBridgePlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            CommandBridgeScript script = plugin.getScript();
            
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Command Bridge v" + CommandBridgeScript.version)
                    .color(Color.CYAN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right(script.getLastStatus())
                    .rightColor(getStatusColor(script.getLastStatus()))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Connection:")
                    .right(script.getConnectionStatus())
                    .rightColor(script.getConnectionStatus().equals("Connected") ? Color.GREEN : Color.RED)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Queue Size:")
                    .right(String.valueOf(script.getQueueSize()))
                    .rightColor(script.getQueueSize() > 0 ? Color.YELLOW : Color.WHITE)
                    .build());

            if (Microbot.getClient().getLocalPlayer() != null) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Player:")
                        .right(Microbot.getClient().getLocalPlayer().getName())
                        .rightColor(Color.WHITE)
                        .build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Location:")
                        .right("(" + Microbot.getClient().getLocalPlayer().getWorldLocation().getX() + 
                               ", " + Microbot.getClient().getLocalPlayer().getWorldLocation().getY() + ")")
                        .rightColor(Color.LIGHT_GRAY)
                        .build());
            }

        } catch (Exception ex) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Error:")
                    .right("Overlay error")
                    .rightColor(Color.RED)
                    .build());
        }

        return super.render(graphics);
    }

    private Color getStatusColor(String status) {
        if (status == null) return Color.WHITE;
        
        String lowerStatus = status.toLowerCase();
        if (lowerStatus.contains("error") || lowerStatus.contains("failed")) {
            return Color.RED;
        } else if (lowerStatus.contains("executing") || lowerStatus.contains("walking") || lowerStatus.contains("banking")) {
            return Color.YELLOW;
        } else if (lowerStatus.contains("idle") || lowerStatus.contains("connected")) {
            return Color.GREEN;
        } else {
            return Color.WHITE;
        }
    }
}'''

print("Enhanced plugin files created!")
print("1. CommandBridgePlugin.java - Length:", len(command_bridge_plugin))
print("2. CommandBridgeConfig.java - Length:", len(command_bridge_config))  
print("3. CommandBridgeOverlay.java - Length:", len(command_bridge_overlay))

# Save all files
with open('CommandBridgePlugin_Enhanced.java', 'w') as f:
    f.write(command_bridge_plugin)
    
with open('CommandBridgeConfig_Enhanced.java', 'w') as f:
    f.write(command_bridge_config)
    
with open('CommandBridgeOverlay_Enhanced.java', 'w') as f:
    f.write(command_bridge_overlay)

print("\nAll files saved with _Enhanced suffix for reference!")