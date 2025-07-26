# Create a comprehensive setup guide

setup_guide = '''# Microbot CommandBridge - Complete Setup Guide

## Overview

This system allows you to control multiple Microbot clients from a central server. Each client runs the CommandBridge plugin and periodically checks for commands from your controller server.

## Architecture

```
[Your Controller Server] ←→ [HTTP REST API] ←→ [CommandBridge Plugin on each Microbot Client]
```

## Step 1: Set Up the Controller Server

### Prerequisites
- Python 3.7+ installed
- pip (Python package manager)

### Installation

1. **Install required Python packages:**
```bash
pip install flask requests
```

2. **Save the controller server code** to `MicrobotController.py` (provided in the assets)

3. **Run the server:**
```bash
python MicrobotController.py
```

The server will start on `http://localhost:8080`

### Server Features
- REST API for sending commands
- Client status monitoring
- Command history tracking
- Simple web interface (optional)

## Step 2: Install CommandBridge Plugin

### Plugin Files Structure
```
runelite-client/src/main/java/net/runelite/client/plugins/microbot/commandbridge/
├── CommandBridgeScript.java
├── CommandBridgePlugin.java
├── CommandBridgeConfig.java
└── CommandBridgeOverlay.java
```

### Installation Steps

1. **Replace your existing files** with the enhanced versions provided:
   - Use `CommandBridgeScript_Enhanced.java` as your `CommandBridgeScript.java`
   - Use `CommandBridgePlugin_Enhanced.java` as your `CommandBridgePlugin.java`
   - Use `CommandBridgeConfig_Enhanced.java` as your `CommandBridgeConfig.java` 
   - Use `CommandBridgeOverlay_Enhanced.java` as your `CommandBridgeOverlay.java`

2. **Compile the Microbot project** following the standard Microbot build process

3. **Configure each client:**
   - Server URL: `http://your-server-ip:8080`
   - Client ID: Unique identifier (e.g., `bot_1`, `bot_2`, etc.)

## Step 3: Basic Testing

### 1. Start the Controller Server
```bash
python MicrobotController.py
```

### 2. Launch Microbot Clients
- Start 2-3 Microbot instances
- Enable the CommandBridge plugin on each
- Configure with unique Client IDs: `bot_1`, `bot_2`, `bot_3`
- Set Server URL to `http://localhost:8080`

### 3. Test Basic Commands

#### Using Python:
```python
import requests

# Send a simple walk command
response = requests.post('http://localhost:8080/api/commands/send', json={
    "action": "walk_to_bank",
    "parameters": {"bank": "GRAND_EXCHANGE"},
    "target_clients": ["bot_1"]
})
print(response.json())
```

#### Using cURL:
```bash
curl -X POST http://localhost:8080/api/commands/send \\
  -H "Content-Type: application/json" \\
  -d '{
    "action": "get_status",
    "parameters": {},
    "target_clients": ["bot_1", "bot_2"]
  }'
```

### 4. Check Client Status
```bash
curl http://localhost:8080/api/clients
```

## Step 4: Scaling to 100+ Clients

### Client Naming Convention
Use a systematic naming approach:
```
Mining Operation: miner_01, miner_02, ..., miner_20
Woodcutting: wc_01, wc_02, ..., wc_15
Combat Training: combat_01, combat_02, ..., combat_10
Grand Exchange: ge_01, ge_02, ..., ge_05
```

### Server Configuration for High Load

#### Option 1: Single Server (up to ~50 clients)
- Default setup works fine
- Monitor server CPU/memory usage

#### Option 2: Load Balanced Setup (50+ clients)
- Run multiple server instances on different ports
- Use nginx or similar for load balancing
- Consider using Redis for shared command storage

### Example Production Setup Script

```python
#!/usr/bin/env python3
"""
Production deployment script for 100 clients
"""
import requests
import time
from concurrent.futures import ThreadPoolExecutor

class MicrobotController:
    def __init__(self, server_url="http://localhost:8080"):
        self.server_url = server_url
        
    def send_command_batch(self, action, parameters, client_groups):
        """Send command to multiple client groups simultaneously"""
        with ThreadPoolExecutor(max_workers=10) as executor:
            futures = []
            
            for group_name, client_ids in client_groups.items():
                future = executor.submit(
                    self.send_command, action, parameters, client_ids
                )
                futures.append((group_name, future))
            
            results = {}
            for group_name, future in futures:
                try:
                    results[group_name] = future.result(timeout=30)
                except Exception as e:
                    results[group_name] = {"error": str(e)}
            
            return results
    
    def send_command(self, action, parameters, target_clients):
        """Send command to specific clients"""
        response = requests.post(f"{self.server_url}/api/commands/send", 
            json={
                "action": action,
                "parameters": parameters,
                "target_clients": target_clients
            },
            timeout=10
        )
        return response.json()
    
    def get_all_clients(self):
        """Get all connected clients"""
        response = requests.get(f"{self.server_url}/api/clients")
        return response.json()
    
    def setup_mining_operation(self, client_count=20):
        """Setup large-scale mining operation"""
        client_groups = {
            "miners": [f"miner_{i:02d}" for i in range(1, client_count + 1)]
        }
        
        print(f"Setting up mining operation for {client_count} clients...")
        
        # Sequence of commands
        commands = [
            ("walk_to_bank", {"bank": "EDGEVILLE"}),
            ("open_bank", {}),
            ("deposit_all", {}),
            ("withdraw_item", {"item": "Rune pickaxe", "quantity": 1}),
            ("close_bank", {}),
            ("equip_item", {"item": "Rune pickaxe"}),
            ("walk_to_coordinates", {"x": 3046, "y": 9776, "plane": 0})
        ]
        
        for i, (action, parameters) in enumerate(commands):
            print(f"Step {i+1}/{len(commands)}: {action}")
            results = self.send_command_batch(action, parameters, client_groups)
            print(f"Results: {results}")
            time.sleep(3)  # Wait between steps
        
        print("Mining operation setup complete!")

# Usage example
if __name__ == "__main__":
    controller = MicrobotController()
    
    # Check connected clients
    clients = controller.get_all_clients()
    print(f"Connected clients: {clients['count']}")
    
    # Setup mining for 20 clients
    controller.setup_mining_operation(20)
```

## Step 5: Advanced Features

### Command Queuing
The system automatically queues commands for each client. If a client is busy, commands wait in queue.

### Error Handling
Each command returns status information:
```json
{
    "command_id": "abc123",
    "status": "success|error|pending",
    "message": "Command executed successfully",
    "timestamp": 1234567890
}
```

### Status Monitoring
Clients automatically send status updates including:
- Player location
- Health and run energy
- Inventory status
- Current activity
- Connection status

### Safety Features
Built-in safety checks:
- Command timeouts
- Anti-ban delays
- Error recovery
- Connection monitoring

## Step 6: Monitoring and Maintenance

### Health Checks
```python
import requests

def check_system_health():
    # Check server
    try:
        response = requests.get('http://localhost:8080/api/clients', timeout=5)
        clients = response.json()
        print(f"Server OK - {clients['count']} clients connected")
    except:
        print("Server ERROR - Cannot connect")
    
    # Check client responsiveness
    test_command = {
        "action": "get_status",
        "parameters": {},
        "target_clients": ["bot_1"]  # Test with one client
    }
    
    try:
        response = requests.post('http://localhost:8080/api/commands/send', 
                               json=test_command, timeout=10)
        print("Command system OK")
    except:
        print("Command system ERROR")

# Run health check every minute
import schedule
schedule.every(1).minutes.do(check_system_health)
```

### Log Analysis
Monitor the Microbot client logs for:
- Command execution times
- Error messages
- Connection issues
- Performance bottlenecks

### Performance Optimization
- Adjust poll intervals based on load
- Use command batching for efficiency
- Monitor server resource usage
- Scale horizontally when needed

## Troubleshooting

### Common Issues

1. **Clients not connecting:**
   - Check server URL configuration
   - Verify firewall settings
   - Ensure server is running

2. **Commands not executing:**
   - Check client logs for errors
   - Verify command syntax
   - Check if client is logged in

3. **Slow response times:**
   - Reduce poll interval
   - Check server performance
   - Consider load balancing

4. **Memory issues with many clients:**
   - Increase JVM heap size
   - Use database instead of in-memory storage
   - Implement cleanup routines

### Debug Mode
Enable debug logging in the plugin configuration to see detailed execution information.

### Support
- Check the Microbot Discord for community support
- Review the Microbot GitHub repository for updates
- Monitor system logs for error patterns

## Security Considerations

### For Production Use:
1. **Add authentication** to the controller server
2. **Use HTTPS** instead of HTTP
3. **Implement rate limiting** to prevent abuse
4. **Validate all input** parameters
5. **Use firewall rules** to restrict access
6. **Monitor for suspicious activity**

Example secure server setup:
```python
from flask import Flask, request, jsonify
from functools import wraps

def require_api_key(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        api_key = request.headers.get('X-API-Key')
        if api_key != 'your-secret-api-key':
            return jsonify({'error': 'Invalid API key'}), 401
        return f(*args, **kwargs)
    return decorated_function

@app.route('/api/commands/send', methods=['POST'])
@require_api_key
def send_command():
    # Your existing code here
    pass
```

## Next Steps

1. **Start small** - Test with 2-3 clients first
2. **Gradually scale** - Add more clients as you gain confidence
3. **Monitor performance** - Watch for bottlenecks
4. **Automate common tasks** - Create preset command sequences
5. **Build dashboards** - Create monitoring interfaces
6. **Document your setup** - Keep notes on what works

This system gives you complete control over multiple Microbot clients while maintaining simplicity and reliability. The modular design allows you to add new commands and features as needed.
'''

print("Setup Guide created!")
print("Length:", len(setup_guide), "characters")

# Save the setup guide
with open('SetupGuide.md', 'w') as f:
    f.write(setup_guide)

print("File saved as SetupGuide.md")