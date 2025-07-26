package net.runelite.client.plugins.microbot.VaultLooter;

import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Script;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class VaultLooterScript extends Script {

    private static final int EMOTE_CONTAINER = 216;
    private static final int EMOTE_BUTTON_GROUP_ID = 164;
    private static final int EMOTE_BUTTON_CHILD_INDEX = 48;

    public boolean run(VaultLooterConfig config) {
        try {
            System.out.println("VaultLooterScript started!"); // Debug
            mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
                try {
                    if (!super.run()) {
                        System.out.println("Super run returned false");
                        return;
                    }
                    System.out.println("Checking player location");
                    // walk to outside of vault 3192 9825 outside 3191 9825 outside2
                    // Define set points for vault location
                    final WorldPoint VAULT_OUTSIDE_POINT_1 = new WorldPoint(3192, 9825, 0);
                    final WorldPoint VAULT_OUTSIDE_POINT_2 = new WorldPoint(3191, 9825, 0);

                    if (!Rs2Player.isNearArea(VAULT_OUTSIDE_POINT_1, 0) && !Rs2Player.isNearArea(VAULT_OUTSIDE_POINT_2, 0)) {
                        System.out.println("Player is not near vault outside points");
                        // Alternate between two points to add some randomness
                        WorldPoint targetPoint = Rs2Player.getWorldLocation().distanceTo(VAULT_OUTSIDE_POINT_1) < 
                                                 Rs2Player.getWorldLocation().distanceTo(VAULT_OUTSIDE_POINT_2) 
                                                 ? VAULT_OUTSIDE_POINT_1 : VAULT_OUTSIDE_POINT_2;
                        System.out.println("Walking to target point: " + targetPoint);
                        int maxAttempts = 3;
                        for (int attempt = 0; attempt < maxAttempts; attempt++) {
                            System.out.println("Attempt " + (attempt+1) + " to walk to " + targetPoint);
                            Rs2Walker.walkCanvas(targetPoint);
                            // Wait for up to 5 seconds per attempt
                            while (!Rs2Player.getWorldLocation().equals(targetPoint)) {
                                sleep(100, 500);
                            }
                            System.out.println("Arrived at target point? " + Rs2Player.getWorldLocation().equals(targetPoint));
                            if (Rs2Player.getWorldLocation().equals(targetPoint)) {
                                
                            }
                        }
                        System.out.println("Starting emote sequence");
                        // once on the tile outide, perform the set of emotes.
                        String[] emotes = {"Yes", "No", "Bow", "Dance", "Panic", "No", "Beckon", "Laugh", "Shrug", "Cry", "Spin", "Yes", "Think", "Dance", "Blow Kiss", "Wave", "Bow", "Panic", "Headbang", "Jump for Joy", "Angry"};
                        // Open emote panel if not already open
                        if (Rs2Widget.getWidget(EMOTE_CONTAINER) == null) {
                            System.out.println("Opening emote panel");
                            Rs2Widget.clickWidget(EMOTE_BUTTON_GROUP_ID, EMOTE_BUTTON_CHILD_INDEX);
                            sleep(600);
                        }
                        for (String emote: emotes) {
                            System.out.println("Performing emote: " + emote);
                            Widget emoteContainer = Rs2Widget.getWidget(EMOTE_CONTAINER);
                            if (emoteContainer == null) {
                                System.out.println("Emote container not found");
                                break;
                            }
                            Widget emoteWidget = Rs2Widget.findWidget(emote, Arrays.asList(emoteContainer.getChildren()));
                            if (emoteWidget == null) {
                                System.out.println("Emote widget not found: " + emote);
                                continue;
                            }
                            Rs2Widget.clickWidget(emoteWidget);
                            // Wait for the animation to start
                            sleepUntilTrue(() -> Rs2Player.isAnimating(), 100, 5000);
                            // Wait for the animation to finish
                            sleepUntilTrue(() -> !Rs2Player.isAnimating(), 100, 5000);
                        }
                        System.out.println("Finished emote sequence");
                        // After emotes, continue the chat dialog with the wizard NPC
                        while (Rs2Dialogue.isInDialogue()) {
                            Rs2Dialogue.clickContinue();
                            sleep(600, 1000);
                        }
                    } else {
                        System.out.println("Player is already near vault outside points");
                    }
                } catch (Exception ex) {
                    System.out.println("Script error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }, 0, 1000, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Invalid UUID string")) {
                System.out.println("Ignoring session client error");
                return true;
            }
            throw e;
        }
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
