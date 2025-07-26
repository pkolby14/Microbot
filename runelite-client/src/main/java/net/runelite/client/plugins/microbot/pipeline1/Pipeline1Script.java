package net.runelite.client.plugins.microbot.pipeline1;

import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;


public class Pipeline1Script extends Script {
    private static final WorldPoint TARGET_POINT = new WorldPoint(2989, 3177, 0);

    @Override
    public boolean run() {
        // Open bank via nearest banker NPC
        Rs2Bank.openBank(Rs2Npc.getBankerNPC());
        // Check and prepare essential items
        if (!Rs2Inventory.hasItem(ItemID.SMALL_FISHING_NET)) {
            Rs2Bank.withdrawX(ItemID.COINS_995, 10);
            Rs2Bank.closeBank();
            Rs2Shop.openShop("Gerrant", true);
            Rs2Shop.buyItem("Small fishing net", "1");
            Rs2Shop.closeShop();
            Rs2Bank.openBank(Rs2Npc.getBankerNPC());
        }
        // Withdraw required items if missing
        if (!Rs2Inventory.hasItem(ItemID.SMALL_FISHING_NET)) {
            Rs2Bank.withdrawX(ItemID.SMALL_FISHING_NET, 1);
        }
        if (!Rs2Inventory.hasItem(ItemID.TINDERBOX)) {
            Rs2Bank.withdrawX(ItemID.TINDERBOX, 1);
        }
        if (!Rs2Inventory.hasItem(ItemID.BRONZE_AXE)) {
            Rs2Bank.withdrawX(ItemID.BRONZE_AXE, 1);
        }
        if (!Rs2Inventory.hasItem(ItemID.WOODEN_SHIELD)) {
            Rs2Bank.withdrawX(ItemID.WOODEN_SHIELD, 1);
        }
        // Close bank
        Rs2Bank.closeBank();
        // Walk to target location
        Rs2Walker.walkTo(TARGET_POINT);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
