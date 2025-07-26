#!/usr/bin/env python3
"""
Simple Command Controller Server for Microbot CommandBridge
This server manages commands for multiple Microbot clients
"""

from flask import Flask, request, jsonify
import json
import uuid
import time
from datetime import datetime
from collections import defaultdict

app = Flask(__name__)

# In-memory storage (use database for production)
clients = {}  # client_id -> client_info
commands = defaultdict(list)  # client_id -> [commands]
command_history = []  # All executed commands
client_status = {}  # client_id -> latest status

class CommandController:

    @staticmethod
    def create_command(action, parameters=None, target_clients=None, command_id=None):
        """Create a properly formatted command"""
        if command_id is None:
            command_id = str(uuid.uuid4())[:8]

        command = {
            "command_id": command_id,
            "action": action,
            "parameters": parameters or {},
            "target_clients": target_clients or [],
            "created_at": time.time(),
            "status": "pending"
        }
        return command

# === CLIENT MANAGEMENT ===

@app.route('/api/clients', methods=['GET'])
def get_clients():
    """Get all connected clients"""
    return jsonify({
        "clients": clients,
        "count": len(clients)
    })

@app.route('/api/clients/<client_id>/status', methods=['GET'])
def get_client_status(client_id):
    """Get specific client status"""
    if client_id in client_status:
        return jsonify(client_status[client_id])
    return jsonify({"error": "Client not found"}), 404

# === COMMAND ENDPOINTS ===

@app.route('/api/commands/<client_id>', methods=['GET'])
def get_commands(client_id):
    """Get pending commands for a specific client"""
    if client_id not in commands or not commands[client_id]:
        return jsonify(None)  # No commands

    # Return the first pending command
    command = commands[client_id].pop(0)
    return jsonify(command)

@app.route('/api/commands/send', methods=['POST'])
def send_command():
    """Send a command to one or more clients"""
    data = request.json

    action = data.get('action')
    parameters = data.get('parameters', {})
    target_clients = data.get('target_clients', [])

    if not action:
        return jsonify({"error": "Action required"}), 400

    if not target_clients:
        return jsonify({"error": "Target clients required"}), 400

    # Create command for each target client
    results = []
    for client_id in target_clients:
        command = CommandController.create_command(action, parameters, [client_id])
        commands[client_id].append(command)
        results.append({
            "client_id": client_id,
            "command_id": command["command_id"],
            "status": "queued"
        })

    return jsonify({
        "message": f"Command '{action}' sent to {len(target_clients)} clients",
        "results": results
    })

@app.route('/api/commands/broadcast', methods=['POST'])
def broadcast_command():
    """Send a command to all connected clients"""
    data = request.json

    action = data.get('action')
    parameters = data.get('parameters', {})

    if not action:
        return jsonify({"error": "Action required"}), 400

    # Send to all clients that have sent status recently (last 30 seconds)
    current_time = time.time()
    active_clients = [
        client_id for client_id, status in client_status.items()
        if current_time - status.get('timestamp', 0) < 30000  # 30 seconds in ms
    ]

    results = []
    for client_id in active_clients:
        command = CommandController.create_command(action, parameters, [client_id])
        commands[client_id].append(command)
        results.append({
            "client_id": client_id,
            "command_id": command["command_id"],
            "status": "queued"
        })

    return jsonify({
        "message": f"Command '{action}' broadcast to {len(active_clients)} active clients",
        "results": results
    })

# === STATUS AND RESULTS ===

@app.route('/api/status', methods=['POST'])
def receive_status():
    """Receive status update from client"""
    data = request.json
    client_id = data.get('client_id')

    if not client_id:
        return jsonify({"error": "Client ID required"}), 400

    # Update client info
    clients[client_id] = {
        "last_seen": datetime.now().isoformat(),
        "player_name": data.get('player_name'),
        "world_location": {
            "x": data.get('world_location_x'),
            "y": data.get('world_location_y'),
            "plane": data.get('world_location_plane')
        }
    }

    # Update status
    client_status[client_id] = data

    return jsonify({"status": "received"})

@app.route('/api/results', methods=['POST'])
def receive_results():
    """Receive command execution results from client"""
    data = request.json

    # Store in command history
    command_history.append({
        **data,
        "received_at": datetime.now().isoformat()
    })

    print(f"Command result from {data.get('client_id')}: {data.get('status')} - {data.get('message')}")

    return jsonify({"status": "received"})

if __name__ == '__main__':
    print("Starting Microbot Command Controller Server...")
    print("API Endpoints available at: http://localhost:8080")
    app.run(host='0.0.0.0', port=8080, debug=True)
