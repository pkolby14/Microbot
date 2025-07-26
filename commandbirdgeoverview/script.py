# Let me create the complete CommandBridge plugin files based on the user's requirements

# First, let's create the enhanced CommandBridgeScript.java with the core functionality
command_bridge_script = '''package net.runelite.client.plugins.microbot.commandbridge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CommandBridgeScript extends Script {

    public static double version = 1.0;
    private CommandBridgeConfig config;
    private Gson gson = new Gson();
    private JsonParser jsonParser = new JsonParser();
    
    // Command queue for processing incoming commands
    private BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();
    
    // Connection status
    private boolean isConnected = false;
    private String lastStatus = "Disconnected";
    
    public boolean run(CommandBridgeConfig config) {
        this.config = config;
        Microbot.enableAutoRunOn = false;
        
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run()) return;
                if (!Microbot.isLoggedIn()) return;

                long startTime = System.currentTimeMillis();

                // Check for incoming commands from server
                pollForCommands();
                
                // Process any queued commands
                processCommands();
                
                // Send status update
                sendStatusUpdate();

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("CommandBridge loop time: " + totalTime + "ms");

            } catch (Exception ex) {
                System.out.println("CommandBridge error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        
        return true;
    }
    
    /**
     * Poll the central server for new commands
     */
    private void pollForCommands() {
        if (config.serverUrl() == null || config.serverUrl().isEmpty()) {
            return;
        }
        
        try {
            URL url = new URL(config.serverUrl() + "/api/commands/" + config.clientId());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = reader.readLine();
                reader.close();
                
                if (response != null && !response.isEmpty() && !response.equals("null")) {
                    commandQueue.offer(response);
                    isConnected = true;
                }
            }
        } catch (Exception ex) {
            isConnected = false;
            System.out.println("Error polling for commands: " + ex.getMessage());
        }
    }
    
    /**
     * Process all queued commands
     */
    private void processCommands() {
        String command;
        while ((command = commandQueue.poll()) != null) {
            try {
                JsonObject jsonCommand = jsonParser.parse(command).getAsJsonObject();
                executeCommand(jsonCommand);
            } catch (Exception ex) {
                System.out.println("Error processing command: " + ex.getMessage());
                sendCommandResult("error", "Failed to process command: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Execute a single command
     */
    private void executeCommand(JsonObject command) {
        String action = command.get("action").getAsString();
        String commandId = command.has("command_id") ? command.get("command_id").getAsString() : "unknown";
        
        lastStatus = "Executing: " + action;
        System.out.println("Executing command: " + action + " (ID: " + commandId + ")");
        
        try {
            switch (action.toLowerCase()) {
                case "walk_to_coordinates":
                    handleWalkToCoordinates(command);
                    break;
                case "walk_to_bank":
                    handleWalkToBank(command);
                    break;
                case "open_bank":
                    handleOpenBank(command);
                    break;
                case "close_bank":
                    handleCloseBank(command);
                    break;
                case "deposit_all":
                    handleDepositAll(command);
                    break;
                case "deposit_item":
                    handleDepositItem(command);
                    break;
                case "withdraw_item":
                    handleWithdrawItem(command);
                    break;
                case "equip_item":
                    handleEquipItem(command);
                    break;
                case "unequip_item":
                    handleUnequipItem(command);
                    break;
                case "get_status":
                    handleGetStatus(command);
                    break;
                default:
                    sendCommandResult(commandId, "error", "Unknown command: " + action);
                    return;
            }
            
            sendCommandResult(commandId, "success", "Command executed successfully");
            lastStatus = "Idle";
            
        } catch (Exception ex) {
            System.out.println("Error executing command " + action + ": " + ex.getMessage());
            sendCommandResult(commandId, "error", "Command failed: " + ex.getMessage());
            lastStatus = "Error: " + ex.getMessage();
        }
    }
    
    // === COMMAND HANDLERS ===
    
    private void handleWalkToCoordinates(JsonObject command) {
        int x = command.get("x").getAsInt();
        int y = command.get("y").getAsInt();
        int plane = command.has("plane") ? command.get("plane").getAsInt() : 0;
        
        WorldPoint destination = new WorldPoint(x, y, plane);
        System.out.println("Walking to coordinates: " + destination);
        
        Rs2Walker.walkTo(destination);
        
        // Wait for arrival with timeout
        int timeout = command.has("timeout") ? command.get("timeout").getAsInt() : 30000;
        sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(destination) <= 5, timeout);
    }
    
    private void handleWalkToBank(JsonObject command) {
        String bankName = command.has("bank") ? command.get("bank").getAsString() : "GRAND_EXCHANGE";
        
        BankLocation bankLocation;
        try {
            bankLocation = BankLocation.valueOf(bankName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            bankLocation = BankLocation.GRAND_EXCHANGE; // Default fallback
        }
        
        System.out.println("Walking to bank: " + bankLocation);
        Rs2Walker.walkTo(bankLocation.getWorldPoint());
        
        // Wait for arrival
        sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(bankLocation.getWorldPoint()) <= 10, 30000);
    }
    
    private void handleOpenBank(JsonObject command) {
        System.out.println("Opening bank");
        if (!Rs2Bank.openBank()) {
            throw new RuntimeException("Failed to open bank");
        }
        
        // Wait for bank to open
        sleepUntil(() -> Rs2Bank.isOpen(), 10000);
    }
    
    private void handleCloseBank(JsonObject command) {
        System.out.println("Closing bank");
        Rs2Bank.closeBank();
        
        // Wait for bank to close
        sleepUntil(() -> !Rs2Bank.isOpen(), 5000);
    }
    
    private void handleDepositAll(JsonObject command) {
        System.out.println("Depositing all items");
        if (!Rs2Bank.isOpen()) {
            throw new RuntimeException("Bank is not open");
        }
        
        Rs2Bank.depositAll();
        
        // Wait for inventory to empty
        sleepUntil(() -> Rs2Inventory.isEmpty(), 10000);
    }
    
    private void handleDepositItem(JsonObject command) {
        String itemName = command.get("item").getAsString();
        int quantity = command.has("quantity") ? command.get("quantity").getAsInt() : -1; // -1 means all
        
        System.out.println("Depositing item: " + itemName + " (quantity: " + quantity + ")");
        
        if (!Rs2Bank.isOpen()) {
            throw new RuntimeException("Bank is not open");
        }
        
        if (quantity == -1) {
            Rs2Bank.depositAll(itemName);
        } else {
            Rs2Bank.depositX(itemName, quantity);
        }
        
        sleep(1000); // Allow time for deposit
    }
    
    private void handleWithdrawItem(JsonObject command) {
        String itemName = command.get("item").getAsString();
        int quantity = command.has("quantity") ? command.get("quantity").getAsInt() : 1;
        
        System.out.println("Withdrawing item: " + itemName + " (quantity: " + quantity + ")");
        
        if (!Rs2Bank.isOpen()) {
            throw new RuntimeException("Bank is not open");
        }
        
        if (quantity == 1) {
            Rs2Bank.withdrawOne(itemName);
        } else {
            Rs2Bank.withdrawX(itemName, quantity);
        }
        
        sleep(1000); // Allow time for withdrawal
    }
    
    private void handleEquipItem(JsonObject command) {
        String itemName = command.get("item").getAsString();
        
        System.out.println("Equipping item: " + itemName);
        
        if (!Rs2Inventory.hasItem(itemName)) {
            throw new RuntimeException("Item not found in inventory: " + itemName);
        }
        
        Rs2Inventory.interact(itemName, "Wear", "Wield", "Equip");
        
        sleep(1000); // Allow time for equipping
    }
    
    private void handleUnequipItem(JsonObject command) {
        String itemName = command.get("item").getAsString();
        
        System.out.println("Unequipping item: " + itemName);
        
        if (!Rs2Equipment.hasEquipped(itemName)) {
            throw new RuntimeException("Item not equipped: " + itemName);
        }
        
        Rs2Equipment.interact(itemName, "Remove");
        
        sleep(1000); // Allow time for unequipping
    }
    
    private void handleGetStatus(JsonObject command) {
        // Status is handled by sendStatusUpdate method
        System.out.println("Status requested");
    }
    
    // === COMMUNICATION METHODS ===
    
    private void sendCommandResult(String commandId, String status, String message) {
        if (config.serverUrl() == null || config.serverUrl().isEmpty()) {
            return;
        }
        
        try {
            JsonObject result = new JsonObject();
            result.addProperty("client_id", config.clientId());
            result.addProperty("command_id", commandId);
            result.addProperty("status", status);
            result.addProperty("message", message);
            result.addProperty("timestamp", System.currentTimeMillis());
            
            sendToServer("/api/results", result.toString());
        } catch (Exception ex) {
            System.out.println("Error sending command result: " + ex.getMessage());
        }
    }
    
    private void sendCommandResult(String status, String message) {
        sendCommandResult("unknown", status, message);
    }
    
    private void sendStatusUpdate() {
        if (config.serverUrl() == null || config.serverUrl().isEmpty()) {
            return;
        }
        
        try {
            JsonObject status = new JsonObject();
            status.addProperty("client_id", config.clientId());
            status.addProperty("player_name", Rs2Player.getLocalPlayer().getName());
            status.addProperty("world_location_x", Rs2Player.getWorldLocation().getX());
            status.addProperty("world_location_y", Rs2Player.getWorldLocation().getY());
            status.addProperty("world_location_plane", Rs2Player.getWorldLocation().getPlane());
            status.addProperty("health", Rs2Player.getBoostedSkillLevel(3)); // Hitpoints
            status.addProperty("run_energy", Rs2Player.getRunEnergy());
            status.addProperty("is_idle", Rs2Player.isIdle());
            status.addProperty("is_moving", Rs2Player.isMoving());
            status.addProperty("is_animating", Rs2Player.isAnimating());
            status.addProperty("inventory_count", Rs2Inventory.count());
            status.addProperty("is_bank_open", Rs2Bank.isOpen());
            status.addProperty("status", lastStatus);
            status.addProperty("connected", isConnected);
            status.addProperty("timestamp", System.currentTimeMillis());
            
            sendToServer("/api/status", status.toString());
        } catch (Exception ex) {
            System.out.println("Error sending status update: " + ex.getMessage());
        }
    }
    
    private void sendToServer(String endpoint, String jsonData) {
        try {
            URL url = new URL(config.serverUrl() + endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonData.getBytes());
            outputStream.flush();
            outputStream.close();
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Server responded with code: " + responseCode);
            }
            
        } catch (IOException ex) {
            System.out.println("Error sending data to server: " + ex.getMessage());
        }
    }
    
    // === UTILITY METHODS ===
    
    public String getConnectionStatus() {
        return isConnected ? "Connected" : "Disconnected";
    }
    
    public String getLastStatus() {
        return lastStatus;
    }
    
    public int getQueueSize() {
        return commandQueue.size();
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
        System.out.println("CommandBridge script shutting down");
    }
}'''

print("CommandBridgeScript.java created successfully!")
print("Length:", len(command_bridge_script), "characters")

# Save to a file for user reference
with open('CommandBridgeScript_Enhanced.java', 'w') as f:
    f.write(command_bridge_script)
    
print("File saved as CommandBridgeScript_Enhanced.java")