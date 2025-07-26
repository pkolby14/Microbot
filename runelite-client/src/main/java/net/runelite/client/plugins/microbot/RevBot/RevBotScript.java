package net.runelite.client.plugins.microbot.RevBot;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;


import java.util.concurrent.TimeUnit;




import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;

import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;

public class RevBotScript extends Script {

    // --- STATE FIELDS ---
    // Add any state-tracking fields here (e.g., timers, flags, etc.)

    public boolean run(RevBotConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                boolean inventoryOk = false;
                boolean equipmentOk = false;
                int setupAttempts = 0;

                // Placeholder for actual item IDs/names from config
                // Example: config.getInventoryItems() and config.getEquipmentItems()
                // For now, let's assume we have a predefined list for testing
                List<Integer> requiredInventoryItemIds = Arrays.asList(2444, 12625, 385, 385, 385, 385, 385, 385, 385, 385, 385, 385, 385);
                List<Integer> requiredEquipmentItemIds = Arrays.asList(1712, 861, 544, 2493, 21816, 2552, 892);

                while ((!inventoryOk || !equipmentOk) && setupAttempts < 3) {
                    Rs2Bank.openBank();
                    if (!Rs2Bank.isOpen()) {
                        Microbot.log("Bank not open, cannot setup inventory/equipment.");
                        sleep(1000);
                        setupAttempts++;
                        continue;
                    }

                    // Deposit all items not in our required list
                    Rs2Bank.depositAllExcept(item -> requiredInventoryItemIds.contains(item.getId()) || requiredEquipmentItemIds.contains(item.getId()));

                    // Withdraw inventory items
                    for (Integer itemId : requiredInventoryItemIds) {
                        if (!Rs2Inventory.hasItem(itemId)) {
                            Rs2Bank.withdrawItem(itemId);
                            sleep(300, 600);
                        }
                    }

                    // Equip items
                    for (Integer itemId : requiredEquipmentItemIds) {
                        if (!Rs2Equipment.isWearing(itemId)) {
                            if (Rs2Inventory.hasItem(itemId)) {
                                Rs2Inventory.wield(itemId);
                                sleep(300, 600);
                            } else {
                                Rs2Bank.withdrawAndEquip(itemId);
                                sleep(300, 600);
                            }
                        }
                    }

                    inventoryOk = requiredInventoryItemIds.stream().allMatch(Rs2Inventory::hasItem);
                    equipmentOk = requiredEquipmentItemIds.stream().allMatch(Rs2Equipment::isWearing);

                    if (!inventoryOk || !equipmentOk) {
                        Microbot.log("Inventory or equipment not set up correctly. Retrying...");
                        sleep(1000);
                    }
                    setupAttempts++;
                }

                if (!inventoryOk || !equipmentOk) {
                    Microbot.log("Failed to set up inventory or equipment after " + setupAttempts + " attempts. Shutting down.");
                    Microbot.pauseAllScripts.set(true);
                    return false;
                }

                // 2. Banking/preparation logic
                // 3. Skull check and acquisition
                if (isAtEdgevilleBank()) {
                    if (!isPlayerSkulled()) {
                        Microbot.log("Not skulled at Edgeville bank, acquiring PK skull...");
                        handlePkSkull();
                        // Wait for skull to appear (simple loop, up to 8s)
                        int skullWait = 0;
                        while (!isPlayerSkulled() && skullWait < 16) {
                            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                            skullWait++;
                        }
                        if (!isPlayerSkulled()) {
                            Microbot.log("Failed to acquire PK skull after Emblem Trader interaction. Stopping script.");
                            shutdown();
                            return;
                        }
                    }
                }
                // 4. Healing at rejuvenation pool
                WorldPoint rejuvenationPool = new WorldPoint(3129, 3632, 0); // Actual pool tile at Ferox
                if (Rs2Walker.walkTo(rejuvenationPool)) {
                    Microbot.status = "Healing at rejuvenation pool";
                    Rs2GameObject.interact("Rejuvenation pool", "Drink");
                    // Wait for heal animation or a short delay
                    try { Thread.sleep(2200); } catch (InterruptedException ignored) {}
                }
                // 5. Walk to Ferox Enclave bank and open bank
if (Rs2Walker.walkTo(BankLocation.FEROX_ENCLAVE.getWorldPoint())) {
    Microbot.status = "Opening Ferox Enclave bank";
    Rs2Bank.openBank();
    try { Thread.sleep(1200); } catch (InterruptedException ignored) {}

    // --- Robust inventory & equipment setup loading ---
    // (Variables already declared at top of method)
    inventoryOk = setup.doesInventoryMatch();
    equipmentOk = setup.doesEquipmentMatch();
    setupAttempts = 0;
    while ((!inventoryOk || !equipmentOk) && setupAttempts < 3) {
        if (!inventoryOk) {
            Microbot.log("Inventory does not match setup, attempting to load from bank...");
            setup.loadInventory();
        }
        if (!equipmentOk) {
            Microbot.log("Equipment does not match setup, attempting to equip...");
            setup.wearEquipment();
        }
        inventoryOk = setup.doesInventoryMatch();
        equipmentOk = setup.doesEquipmentMatch();
        setupAttempts++;
    }

    // --- Fallback: Equip key jewelry/items by flexible name ---
    // Robust fallback for Amulet of Glory (any charged variant)
    if (!isWearingChargedGloryByName()) {
        Microbot.log("Equipping any available charged Amulet of Glory variant...");
        String gloryName = findItemWithPrefixInInventory("amulet of glory(");
        if (gloryName != null) {
            Rs2Inventory.wield(gloryName);
        } else {
            gloryName = findItemWithPrefixInBank("amulet of glory(");
            if (gloryName != null) {
                Rs2Bank.withdrawOne(gloryName);
                final String finalGloryName = gloryName;
                Global.sleepUntil(() -> Rs2Inventory.hasItem(finalGloryName));
                Rs2Inventory.wield(gloryName);
            } else {
                Microbot.log("No charged Amulet of Glory found in bank or inventory!", org.slf4j.event.Level.WARN);
            }
        }
    }
    // Robust fallback for Ring of dueling (any charged variant)
    if (!isWearingChargedDuelingRingByName()) {
        Microbot.log("Equipping any available Ring of dueling variant...");
        String ringName = findItemWithPrefixInInventory("ring of dueling(");
        if (ringName != null) {
            Rs2Inventory.wield(ringName);
        } else {
            ringName = findItemWithPrefixInBank("ring of dueling(");
            if (ringName != null) {
                Rs2Bank.withdrawOne(ringName);
                final String finalRingName = ringName;
                Global.sleepUntil(() -> Rs2Inventory.hasItem(finalRingName));
                Rs2Inventory.wield(ringName);
            } else {
                Microbot.log("No Ring of dueling found in bank or inventory!", org.slf4j.event.Level.WARN);
            }
        }
    }
    // Add more fallback logic for other key items if needed

    // Final check
    inventoryOk = setup.doesInventoryMatch();
    equipmentOk = setup.doesEquipmentMatch();
    if (!inventoryOk || !equipmentOk) {
        Microbot.log("Failed to load 'Revs' inventory/equipment after fallback. Stopping script.");
        shutdown();
        return;
    }
    Microbot.log("Inventory and equipment loaded successfully. Pipeline test complete. Stopping script.");
    shutdown();
    return;
}

                // --- Remaining steps for future implementation ---
                // 6. Combat and looting loop
                // 7. PKer detection and escape logic
                // 8. Death handling logic
                // 9. Anti-ban logic
                // 10. Shutdown/cleanup logic

                // --- End of test block ---

                //long endTime = System.currentTimeMillis();
                //long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0L, 1000L, TimeUnit.MILLISECONDS);
        return true;
    }
    
    // --- JEWELRY DETECTION & WITHDRAWAL HELPERS ---
    /**
     * Checks if any equipped item is a charged Amulet of Glory (by name, e.g. "Amulet of glory(6)").
     * This is robust and works for all charged variants.
     */
    private boolean isWearingChargedGloryByName() {
        return Rs2Equipment.items().stream()
            .anyMatch(item -> item.getName() != null && item.getName().toLowerCase().startsWith("amulet of glory("));
    }

    /**
     * Checks if any equipped item is a charged Ring of Dueling (by name, e.g. "Ring of dueling(8)").
     * This is robust and works for all charged variants.
     */
    private boolean isWearingChargedDuelingRingByName() {
        return Rs2Equipment.items().stream()
            .anyMatch(item -> item.getName() != null && item.getName().toLowerCase().startsWith("ring of dueling("));
    }
    /**
     * Finds the first item in inventory whose name starts with the given prefix (case-insensitive).
     */
    private String findItemWithPrefixInInventory(String prefix) {
        return Rs2Inventory.items()
            .map(item -> item.getName())
            .filter(name -> name != null && name.toLowerCase().startsWith(prefix))
            .findFirst()
            .orElse(null);
    }
    /**
     * Finds the first item in bank whose name starts with the given prefix (case-insensitive).
     */
    private String findItemWithPrefixInBank(String prefix) {
        return Rs2Bank.bankItems().stream()
            .map(item -> item.getName())
            .filter(name -> name != null && name.toLowerCase().startsWith(prefix))
            .findFirst()
            .orElse(null);
    }

    // --- UTILITY METHODS ---
    private boolean isAtEdgevilleBank() {
        // Accept within 7 tiles as "at bank"
        return Rs2Bank.isNearBank(BankLocation.EDGEVILLE, 7);
    }

    private boolean isPlayerSkulled() {
        // SkullIcon.NONE == -1
        Rs2PlayerModel local = Rs2Player.getLocalPlayer();
        return local != null && local.getSkullIcon() != -1;
    }
    
    private void handlePkSkull() {
        // 1. Find Emblem Trader NPC (id: 2824, name: "Emblem Trader")
        // 2. Right-click, select "Skull"
        // 3. Confirm dialogue ("Give me PK skull")
        Rs2NpcModel trader = net.runelite.client.plugins.microbot.util.npc.Rs2Npc.getNpcs(
            npc -> npc.getName() != null && npc.getName().equalsIgnoreCase("Emblem Trader")
        ).findFirst().orElse(null);
        if (trader == null) {
            Microbot.log("Could not find Emblem Trader NPC at Edgeville.");
            return;
        }
        net.runelite.client.plugins.microbot.util.npc.Rs2Npc.interact(trader, "Skull");
        // Wait for dialogue widget and confirm
        long start = System.currentTimeMillis();
        while (!Rs2Dialogue.isInDialogue() && System.currentTimeMillis() - start < 4000) {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        if (Rs2Dialogue.isInDialogue()) {
            // Confirm dialogue: "Give me PK skull"
            Rs2Dialogue.clickCombinationOption("Give me PK skull", true);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}