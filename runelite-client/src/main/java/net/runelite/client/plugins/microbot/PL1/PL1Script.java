package net.runelite.client.plugins.microbot.PL1;

import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.api.coords.WorldPoint;  
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.util.antiban.enums.PlayStyle;
import java.util.concurrent.TimeUnit;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;

import com.google.inject.Singleton;

@Singleton
public class PL1Script extends Script {
    private boolean isDone = false;
    private static final int FISHING_SPOT_ID = 1523;
    private static final String FISHING_ACTION = "Small Net";
    private static final int MAX_RAW_FOOD = 14;
    private final String[] start1Items = {
        "Bronze Axe", 
        "Bronze Sword", 
        "Tinderbox", 
        "Wooden Shield", 
        "Small Fishing Net"
    };

    public boolean run(PL1Config config) {
        reset();
        if (!super.run()) return false;
        
        initializeSettings();
        
        // Check if we already have the fishing net and are at the fishing spot
        if (Rs2Inventory.contains("Small fishing net")) {
            WorldPoint fishingSpot = new WorldPoint(2993, 3169, 0);
            WorldPoint currentPos = Microbot.getClient().getLocalPlayer().getWorldLocation();
            
            if (currentPos.distanceTo(fishingSpot) < 10) {
                System.out.println("Already at fishing spot with net. Starting to fish...");
                RawShrimp();
                return true;
            }
        }
        
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!shouldContinue()) {
                    shutdown();
                    return;
                }
                
                if (!handleBankInteraction()) {
                    return;  // Wait until bank interaction is complete
                }
                
                if (!navigateToTargetPosition()) {
                    return;  // Wait until navigation is complete
                }
                
                RawShrimp();
                
                // If we reach here, both tasks are complete
                isDone = true;
                shutdown();
                
            } catch (Exception ex) {
                System.out.println("Error in script: " + ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        
        return true;
    }

    private void initializeSettings() {
        Microbot.enableAutoRunOn = false; 
        Rs2Antiban.activateAntiban();
        Rs2Antiban.antibanSetupTemplates.applyGeneralBasicSetup();
    }
    
    private void reset() {
        isDone = false;
        initializeSettings();
    }

    private boolean shouldContinue() {
        if (!Microbot.isLoggedIn() || !super.run()) {
            return false;
        }
        return !isInventoryFull();
    }
    
    private boolean isInventoryFull() {
        return Rs2Inventory.isFull();
    }
    private void RawShrimp() {
        if (!Rs2Inventory.contains("Small fishing net")) {
            System.out.println("No fishing net found in inventory");
            return;
        }
        
        System.out.println("Starting fishing process...");
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;
        
        while (attempts < MAX_ATTEMPTS && shouldContinue() && !isInventoryFull()) {
            int inventoryCount = Rs2Inventory.count();
            System.out.println("Attempt " + (attempts + 1) + ": Trying to interact with fishing spot...");
            
            // Try to find and interact with the fishing spot
            boolean success = Rs2GameObject.interact(FISHING_SPOT_ID, "Small Net");
            
            if (!success) {
                System.out.println("Trying to find fishing spot by name...");
                success = Rs2GameObject.interact("Fishing spot", "Small Net");
            }
            
            if (success) {
                System.out.println("Successfully started fishing. Waiting for catch...");
                // Wait for animation to start
                sleepUntil(Rs2Player::isAnimating, 2000);
                
                // Now wait until we catch something or stop animating
                boolean caughtFish = sleepUntil(() -> {
                    boolean caught = Rs2Inventory.count() > inventoryCount;
                    boolean stopped = !Rs2Player.isAnimating();
                    if (caught) System.out.println("Caught a fish!");
                    if (stopped) System.out.println("Stopped fishing");
                    return caught || stopped;
                }, 30000); // Wait up to 30 seconds for a catch
                
                if (caughtFish) {
                    // Successfully caught a fish, reset attempts
                    attempts = 0;
                    Rs2Antiban.actionCooldown();
                    continue;
                }
            } else {
                System.out.println("Could not find fishing spot to interact with");
            }
            
            attempts++;
            if (attempts < MAX_ATTEMPTS) {
                System.out.println("Retrying in 2 seconds...");
                sleep(2000);
            }
        }
        
        if (attempts >= MAX_ATTEMPTS) {
            System.out.println("Failed to fish after " + MAX_ATTEMPTS + " attempts");
        }
    }
        
    

    private void executeScript() {
        if (handleBankInteraction()) {
            isDone = true;
            mainScheduledFuture.cancel(false);
        }
    }

    private boolean handleBankInteraction() {
        // If we have the fishing net, no need to go to the bank
        if (Rs2Inventory.contains("Small fishing net")) {
            System.out.println("Already have fishing net. Ready to fish!");
            return true;
        }
        if (!withdrawItems()) return false;
        Rs2Bank.closeBank();
        
        // Step 2: Now that we have items, go to the target position
        return navigateToTargetPosition();
    }
    
    private boolean navigateToBank() {
        if (Rs2Bank.isOpen()) {
            // Already at the bank and bank is open, no need to find or open again
            return true;
        }
        var bankObject = Rs2Bank.getNearestBank();
        if (bankObject == null) {
            System.out.println("No bank found nearby");
            return false;
        }
        return Rs2Bank.openBank();
    }
    
    private boolean navigateToTargetPosition() {
        WorldPoint targetPosition = new WorldPoint(2993, 3169, 0);
        WorldPoint currentPos = Microbot.getClient().getLocalPlayer().getWorldLocation();
        
        // If already at fishing spot, start fishing
        if (currentPos.distanceTo(targetPosition) < 10) {
            System.out.println("At fishing spot. Checking for fishing net...");
            if (Rs2Inventory.contains("Small fishing net")) {
                System.out.println("Have fishing net. Starting to fish...");
                RawShrimp();
                return true;
            } else {
                System.out.println("No fishing net found. Need to get one from the bank.");
                return false;
            }
        }
        
        // Otherwise, walk to the fishing spot
        System.out.println("Walking to fishing spot at " + targetPosition);
        boolean reached = Rs2Walker.walkTo(targetPosition);
        
        // If we've reached or are close enough, check for fishing net
        if (reached || Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(targetPosition) < 10) {
            System.out.println("Reached fishing spot. Checking for fishing net...");
            if (Rs2Inventory.contains("Small fishing net")) {
                System.out.println("Have fishing net. Starting to fish...");
                RawShrimp();
                return true;
            } else {
                System.out.println("No fishing net found. Need to get one from the bank.");
                return false;
            }
        }
        
        return reached;
    }
    
    private boolean withdrawItems() {
        if (!Rs2Bank.isOpen()) return false;

        for (String itemName : start1Items) {
            if (!Rs2Inventory.contains(itemName)) {
                // Check if the item exists in the bank
                if (!Rs2Bank.hasItem(itemName)) {
                    System.out.println("Item not found in bank: " + itemName + ", skipping.");
                    continue; // Skip to next item
                }
                // Try to withdraw the item
                Rs2Bank.withdrawItem(itemName);
                if (!Rs2Inventory.contains(itemName)) {
                    System.out.println("Failed to withdraw: " + itemName);
                    return false; // Stop if withdrawal fails for other reasons
                }
                sleep(600);
            }
        }
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}