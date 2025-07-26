    // --- Bank Command Handler ---
package net.runelite.client.plugins.microbot.commandbridge;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import org.json.JSONObject;
import org.java_websocket.WebSocket;

public class BankCommandHandlers {
    public static void handleBankCommand(String command, JSONObject json, WebSocket conn) {
        switch (command) {
            case "GetBankItems":
                handleGetBankItems(conn);
                break;
            case "WithdrawItem":
                handleWithdrawItem(json, conn);
                break;
            case "OpenBank":
                handleOpenBank(conn);
                break;
            case "CloseBank":
                handleCloseBank(conn);
                break;
            default:
                conn.send("{\"error\":\"Unknown bank command: " + command + "\"}");
        }
    }

    private static void handleOpenBank(WebSocket conn) {
        boolean success = Rs2Bank.openBank();
        JSONObject resp = new JSONObject();
        resp.put("command", "OpenBankResult");
        resp.put("success", success);
        conn.send(resp.toString());
    }

    private static void handleCloseBank(WebSocket conn) {
        boolean success = Rs2Bank.closeBank();
        JSONObject resp = new JSONObject();
        resp.put("command", "CloseBankResult");
        resp.put("success", success);
        conn.send(resp.toString());
    }

    private static void handleGetBankItems(org.java_websocket.WebSocket conn) {
        // Walk to nearest bank and open if not already open
        boolean bankOpened = net.runelite.client.plugins.microbot.util.bank.Rs2Bank.isOpen() || net.runelite.client.plugins.microbot.util.bank.Rs2Bank.openBank();
        if (!bankOpened) {
            conn.send("{\"error\":\"Could not open bank.\"}");
            return;
        }
        java.util.List<net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel> items = net.runelite.client.plugins.microbot.util.bank.Rs2Bank.bankItems();
        org.json.JSONArray arr = new org.json.JSONArray();
        for (var item : items) {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("id", item.getId());
            obj.put("name", item.getName());
            obj.put("quantity", item.getQuantity());
            obj.put("slot", item.getSlot());
            arr.put(obj);
        }
        org.json.JSONObject resp = new org.json.JSONObject();
        resp.put("command", "BankItems");
        resp.put("items", arr);
        conn.send(resp.toString());
    }

    private static void handleWithdrawItem(JSONObject json, WebSocket conn) {
        String itemName = json.optString("itemName", null);
        int itemId = json.optInt("itemId", -1);
        int amount = json.optInt("amount", 1);
        boolean result = false;
        boolean bankOpened = net.runelite.client.plugins.microbot.util.bank.Rs2Bank.isOpen() || net.runelite.client.plugins.microbot.util.bank.Rs2Bank.openBank();
        if (!bankOpened) {
            conn.send("{\"error\":\"Could not open bank.\"}");
            return;
        }
        if (itemId > 0) {
            if (amount == -1) {
                result = net.runelite.client.plugins.microbot.util.bank.Rs2Bank.withdrawAll(itemId);
            } else if (amount > 1) {
                result = net.runelite.client.plugins.microbot.util.bank.Rs2Bank.withdrawX(itemId, amount);
            } else {
                net.runelite.client.plugins.microbot.util.bank.Rs2Bank.withdrawOne(itemId);
                result = true;
            }
        } else if (itemName != null) {
            if (amount == -1) {
                net.runelite.client.plugins.microbot.util.bank.Rs2Bank.withdrawAll(itemName);
                result = true;
            } else if (amount > 1) {
                net.runelite.client.plugins.microbot.util.bank.Rs2Bank.withdrawX(itemName, amount);
                result = true;
            } else {
                net.runelite.client.plugins.microbot.util.bank.Rs2Bank.withdrawOne(itemName);
                result = true;
            }
        }
        JSONObject resp = new JSONObject();
        resp.put("command", "WithdrawResult");
        resp.put("success", result);
        resp.put("itemName", itemName);
        resp.put("itemId", itemId);
        resp.put("amount", amount);
        conn.send(resp.toString());
    }
}
