package net.runelite.client.plugins.microbot.commandbridge;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.api.coords.WorldPoint;
import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
// Anti-ban imports
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
// If Activity enum exists:
// import net.runelite.client.plugins.microbot.util.antiban.Activity;

@net.runelite.client.plugins.PluginDescriptor(
    name = "Microbot: Command Bridge",
    description = "Bridge for Microbot commands via WebSocket",
    tags = {"microbot", "bridge", "websocket"},
    enabledByDefault = false
)
public class CommandBridgePlugin extends Plugin {
    private CommandBridgeWebSocketServer webSocketServer;
    private final int PORT = 8777;
    private final String uuid = UUID.randomUUID().toString();
    private ScheduledExecutorService executorService;
    private List<String> lastConnectedUsernames = new ArrayList<>();

    public List<String> getLastConnectedUsernames() {
        return lastConnectedUsernames;
    }


    @Inject
    public CommandBridgePlugin() {
    }

    @Override
    protected void startUp() throws Exception {
        // Enforce universal anti-ban at startup
        Rs2AntibanSettings.universalAntiban = true;
        webSocketServer = new CommandBridgeWebSocketServer(PORT, uuid);
        webSocketServer.setWebWalkListener(this::handleWebWalkCommand);
        webSocketServer.setCancelWalkListener(() -> handleCancelWalkCommand());
        webSocketServer.setBankCommandListener(BankCommandHandlers::handleBankCommand);

        webSocketServer.start();
        System.out.println("[CommandBridge] WebSocket started on port " + PORT + ", UUID: " + uuid);
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::broadcastPlayerData, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void shutDown() throws Exception {
        if (webSocketServer != null) {
            try {
                webSocketServer.stop();
                System.out.println("[CommandBridge] WebSocket server stopped.");
            } catch (Exception e) {
                System.out.println("[CommandBridge] Error stopping WebSocket server: " + e.getMessage());
            }
            webSocketServer = null;
        }
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }
    private void broadcastPlayerData() {
        if (!Microbot.isLoggedIn()) return;
        Rs2PlayerModel localPlayer = Rs2Player.getLocalPlayer();
        if (localPlayer == null) return;
        String username = localPlayer.getName();
        double health = Rs2Player.getHealthPercentage();
        int runEnergy = Rs2Player.getRunEnergy();
        WorldPoint worldPoint = localPlayer.getWorldLocation();
        String json = String.format("{\"username\":\"%s\",\"health\":%.2f,\"run_energy\":%d,\"location\":{\"x\":%d,\"y\":%d,\"plane\":%d}}",
                username, health, runEnergy, worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
        webSocketServer.broadcastPlayerData(json, username);
        // update overlay usernames
        updateOverlayUsernames();
    }

    private void updateOverlayUsernames() {
        lastConnectedUsernames.clear();
        lastConnectedUsernames.addAll(webSocketServer.getConnectionUsernames().values());
    }
    // Walk monitor fields
    private volatile boolean walkMonitorActive = false;
    private volatile boolean walkCancelled = false;
    private WorldPoint walkTarget = null;

    /**
     * Cancels any ongoing walk and notifies listeners.
     */
    private void handleCancelWalkCommand() {
        walkCancelled = true;
        walkMonitorActive = false;
        // Optionally notify frontend or log
        System.out.println("[CommandBridge] Walk cancelled by user.");
        sendWalkStatus("cancelled", 0, 0);
    }
    private ScheduledExecutorService walkMonitorExecutor = Executors.newSingleThreadScheduledExecutor();

    private void handleWebWalkCommand(String location, Integer x, Integer y, Integer plane) {
    // Always cancel any previous walk when a new one starts
    walkCancelled = false;
    System.out.println("[CommandBridge] handleWebWalkCommand called. location=" + location + ", x=" + x + ", y=" + y + ", plane=" + plane);
    // Always enable universal anti-ban when walking
    if (!Rs2AntibanSettings.universalAntiban) {
        Rs2AntibanSettings.universalAntiban = true;
        System.out.println("[CommandBridge] Universal anti-ban ENABLED for walk command.");
    }
    WorldPoint wp = null;
    boolean usedSpecialLogic = false;
    // Special handling for nearest bank
    if ("NEAREST_BANK".equals(location)) {
        var nearest = net.runelite.client.plugins.microbot.util.bank.Rs2Bank.getNearestBank();
        if (nearest != null) {
            System.out.println("[CommandBridge] Resolved NEAREST_BANK to BankLocation: " + nearest);
            boolean arrived = net.runelite.client.plugins.microbot.util.bank.Rs2Bank.walkToBank(nearest);
            if (!arrived) {
                int maxTries = 100;
                while (maxTries-- > 0 && !net.runelite.client.plugins.microbot.util.bank.Rs2Bank.isNearBank(nearest, 5) && !walkCancelled) {
                    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                }
            }
            wp = nearest.getWorldPoint();
            System.out.println("[CommandBridge] Arrived near NEAREST_BANK: " + wp);
            usedSpecialLogic = true;
        } else {
            System.out.println("[CommandBridge] Failed to resolve NEAREST_BANK");
        }
    }
    // Special handling for nearest deposit box
    else if ("NEAREST_DEPOSIT_BOX".equals(location)) {
        var nearest = net.runelite.client.plugins.microbot.util.depositbox.Rs2DepositBox.getNearestDepositBox();
        if (nearest != null) {
            System.out.println("[CommandBridge] Resolved NEAREST_DEPOSIT_BOX to DepositBoxLocation: " + nearest);
            // If Rs2DepositBox.walkToDepositBox exists, use it. Otherwise, walk directly to the WorldPoint.
            boolean arrived = false;
            try {
                arrived = net.runelite.client.plugins.microbot.util.depositbox.Rs2DepositBox.walkToDepositBox(nearest);
            } catch (Exception e) {
                // fallback if walkToDepositBox doesn't exist
                net.runelite.client.plugins.microbot.util.walker.Rs2Walker.walkTo(nearest.getWorldPoint());
                arrived = false;
            }
            if (!arrived) {
                int maxTries = 100;
                while (maxTries-- > 0 && nearest.getWorldPoint().distanceTo2D(net.runelite.client.plugins.microbot.Microbot.getClient().getLocalPlayer().getWorldLocation()) > 5 && !walkCancelled) {
                    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                }
            }
            wp = nearest.getWorldPoint();
            System.out.println("[CommandBridge] Arrived near NEAREST_DEPOSIT_BOX: " + wp);
            usedSpecialLogic = true;
        } else {
            System.out.println("[CommandBridge] Failed to resolve NEAREST_DEPOSIT_BOX");
        }
    }
    // Named banks
    else if (location != null && !location.isEmpty()) {
        try {
            net.runelite.client.plugins.microbot.util.bank.enums.BankLocation bankLoc = net.runelite.client.plugins.microbot.util.bank.enums.BankLocation.valueOf(location);
            wp = bankLoc.getWorldPoint();
            System.out.println("[CommandBridge] Resolved BankLocation " + location + " to WorldPoint: " + wp);
        } catch (Exception e) {
            System.out.println("[CommandBridge] Failed to resolve BankLocation for: " + location);
        }
    }
    // Direct coordinates
    if (wp == null && x != null && y != null) {
        int p = (plane != null) ? plane : 0;
        wp = new net.runelite.api.coords.WorldPoint(x, y, p);
        System.out.println("[CommandBridge] Using direct coordinates: " + wp);
    }
    if (wp == null) {
        System.out.println("[CommandBridge] ERROR: No valid WorldPoint for walk command.");
        sendWalkStatus("error_invalid_location", 0, 0);
        return;
    }
    // For non-special cases, walk directly
    if (!usedSpecialLogic) {
        // Direct walk logic with cancellation support
        if (!walkCancelled) {
            net.runelite.client.plugins.microbot.util.walker.Rs2Walker.walkTo(wp);
        }
    }
    startWalkMonitor(wp);
}

private void startWalkMonitor(WorldPoint target) {
    System.out.println("[CommandBridge] startWalkMonitor called. Target=" + target);
    walkMonitorExecutor.shutdownNow();
    walkMonitorExecutor = Executors.newSingleThreadScheduledExecutor();
    walkMonitorExecutor.submit(() -> {
        try {
            // Set anti-ban activity context if available
            // Rs2Antiban.setActivity(Activity.WALKING);
            System.out.println("[CommandBridge] Walk monitor thread started.");
            Rs2Antiban.actionCooldown(); // Human-like delay before starting walk
            System.out.println("[CommandBridge] After actionCooldown, about to walk to target: " + target);
            net.runelite.client.plugins.microbot.util.walker.Rs2Walker.walkTo(target);
            WorldPoint lastPos = null;
            long lastMoveTime = System.currentTimeMillis();
            int totalSteps = net.runelite.client.plugins.microbot.util.walker.Rs2Walker.getDistanceBetween(
                net.runelite.client.plugins.microbot.util.player.Rs2Player.getWorldLocation(), target);
            sendWalkStatus("started", 0, totalSteps);
            while (!walkCancelled) {
                var pos = net.runelite.client.plugins.microbot.util.player.Rs2Player.getWorldLocation();
                int stepsTaken = net.runelite.client.plugins.microbot.util.walker.Rs2Walker.getDistanceBetween(pos, target);
                sendWalkStatus("walking", stepsTaken, totalSteps);
                // Check for arrival BEFORE stuck/retry logic
                if (pos.distanceTo(target) <= 2) {
                    sendWalkStatus("arrived", totalSteps, totalSteps);
                    break;
                }
                boolean moving = net.runelite.client.plugins.microbot.util.player.Rs2Player.isMoving();
                if (!moving && System.currentTimeMillis() - lastMoveTime > 4000) {
                    sendWalkStatus("stuck_retrying", stepsTaken, totalSteps);
                    Rs2Antiban.actionCooldown(); // Human-like delay before retry
                    net.runelite.client.plugins.microbot.util.walker.Rs2Walker.walkTo(target);
                    lastMoveTime = System.currentTimeMillis();
                }
                lastPos = pos;
                try {
                    Thread.sleep(Rs2Random.between(500, 800)); // Human-like random delay for antiban
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            walkMonitorActive = false;
        } catch (Exception e) {
            sendWalkStatus("monitor_error", 0, 0);
        }
    });
}
    private void sendWalkStatus(String status, int stepsTaken, int totalSteps) {
    try {
        String msg = String.format("{\"walk_status\":\"%s\",\"steps_taken\":%d,\"total_steps\":%d}", status, stepsTaken, totalSteps);
        webSocketServer.broadcastPlayerData(msg, "walk_status");
    } catch (Exception ignored) {}
    }
}
