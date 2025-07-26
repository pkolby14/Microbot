package net.runelite.client.plugins.microbot.VaultLooter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
@ConfigGroup("VaultLooter")
public interface VaultLooterConfig extends Config {

    @ConfigItem(
        keyName = "returningToLoot",
        name = "Returning to Loot",
        description = "Set to true if returning to loot, false if leaving to bank"
    )
    default boolean returningToLoot() {
        return true;
    }
}
