package net.runelite.client.plugins.microbot.commandbridge;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandBridgeWebSocketServer extends WebSocketServer {
    private final String uuid;
    private final CopyOnWriteArraySet<WebSocket> connections = new CopyOnWriteArraySet<>();
    private final Map<WebSocket, String> connectionUsernames = new ConcurrentHashMap<>();

    public CommandBridgeWebSocketServer(int port, String uuid) {
        super(new InetSocketAddress("127.0.0.1", port));
        this.uuid = uuid;
    }

    // Called by plugin to update the username for a connection
    public void setUsername(WebSocket conn, String username) {
        if (conn != null && username != null) {
            connectionUsernames.put(conn, username);
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        // Username will be set by plugin after connection
        conn.send("{\"uuid\":\"" + uuid + "\"}");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        connectionUsernames.remove(conn);
    }

    // --- Bank Command Listener ---
    public interface BankCommandListener {
        void onBankCommand(String command, org.json.JSONObject json, WebSocket conn);
    }
    private BankCommandListener bankCommandListener;
    public void setBankCommandListener(BankCommandListener listener) {
        this.bankCommandListener = listener;
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Parse JSON and check for WebWalk/CancelWalk command
        try {
            org.json.JSONObject json = new org.json.JSONObject(message);
            if (json.has("command")) {
                String cmd = json.getString("command");
                if ("WebWalk".equalsIgnoreCase(cmd)) {
                    if (webWalkListener != null) {
                        String location = json.optString("location", null);
                        Integer x = json.has("x") ? json.getInt("x") : null;
                        Integer y = json.has("y") ? json.getInt("y") : null;
                        Integer plane = json.has("plane") ? json.getInt("plane") : null;
                        webWalkListener.onWebWalk(location, x, y, plane);
                    }
                    conn.send("{\"status\":\"WebWalk command received\"}");
                    return;
                }
                if ("CancelWalk".equalsIgnoreCase(cmd)) {
                    if (cancelWalkListener != null) {
                        cancelWalkListener.onCancelWalk();
                    }
                    conn.send("{\"status\":\"CancelWalk command received\"}");
                    return;
                }
                // --- Bank commands ---
                if (bankCommandListener != null && ("GetBankItems".equalsIgnoreCase(cmd) || "WithdrawItem".equalsIgnoreCase(cmd) || "OpenBank".equalsIgnoreCase(cmd) || "CloseBank".equalsIgnoreCase(cmd))) {
                    bankCommandListener.onBankCommand(cmd, json, conn);
                    return;
                }
            }
        } catch (Exception e) {
            // Ignore parse errors, fallback to echo
        }
        conn.send("{\"echo\":" + message + "}");
    }

    // Listener interface for plugin to handle WebWalk
    public interface WebWalkListener {
        void onWebWalk(String location, Integer x, Integer y, Integer plane);
    }
    private WebWalkListener webWalkListener;
    public void setWebWalkListener(WebWalkListener listener) {
        this.webWalkListener = listener;
    }

    // Listener interface for plugin to handle CancelWalk
    public interface CancelWalkListener {
        void onCancelWalk();
    }
    private CancelWalkListener cancelWalkListener;
    public void setCancelWalkListener(CancelWalkListener listener) {
        this.cancelWalkListener = listener;
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port " + getPort());
    }

    public void broadcastPlayerData(String jsonData, String username) {
        for (WebSocket conn : connections) {
            setUsername(conn, username); // Update username mapping
            conn.send(jsonData);
        }
    }

    public CopyOnWriteArraySet<WebSocket> getConnections() {
        return connections;
    }

    public Map<WebSocket, String> getConnectionUsernames() {
        return connectionUsernames;
    }

    public String getUuid() {
        return uuid;
    }
}
