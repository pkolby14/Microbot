package net.runelite.client.plugins.microbot.commandbridge;

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
}