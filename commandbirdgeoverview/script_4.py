# Create command examples and usage documentation

command_examples = '''# Microbot CommandBridge - Command Examples and Usage Guide

## Command Structure

All commands follow this JSON structure:

```json
{
    "action": "command_name",
    "parameters": {
        "param1": "value1",
        "param2": "value2"
    },
    "target_clients": ["bot_1", "bot_2", "bot_3"]
}
```

## Available Commands

### 1. Walking Commands

#### Walk to Specific Coordinates
```json
{
    "action": "walk_to_coordinates",
    "parameters": {
        "x": 3165,
        "y": 3482,
        "plane": 0,
        "timeout": 30000
    },
    "target_clients": ["bot_1"]
}
```

#### Walk to Bank
```json
{
    "action": "walk_to_bank",
    "parameters": {
        "bank": "GRAND_EXCHANGE"
    },
    "target_clients": ["bot_1", "bot_2"]
}
```

Available banks: GRAND_EXCHANGE, EDGEVILLE, VARROCK_EAST, VARROCK_WEST, LUMBRIDGE, DRAYNOR, FALADOR_EAST, FALADOR_WEST, CAMELOT, ARDOUGNE_NORTH, ARDOUGNE_SOUTH, YANILLE, CASTLE_WARS

### 2. Banking Commands

#### Open Bank
```json
{
    "action": "open_bank",
    "parameters": {},
    "target_clients": ["bot_1"]
}
```

#### Close Bank
```json
{
    "action": "close_bank",
    "parameters": {},
    "target_clients": ["bot_1"]
}
```

#### Deposit All Items
```json
{
    "action": "deposit_all",
    "parameters": {},
    "target_clients": ["bot_1"]
}
```

#### Deposit Specific Item
```json
{
    "action": "deposit_item",
    "parameters": {
        "item": "Iron ore",
        "quantity": 10
    },
    "target_clients": ["bot_1"]
}
```

#### Withdraw Item
```json
{
    "action": "withdraw_item",
    "parameters": {
        "item": "Lobster",
        "quantity": 5
    },
    "target_clients": ["bot_1"]
}
```

### 3. Equipment Commands

#### Equip Item
```json
{
    "action": "equip_item",
    "parameters": {
        "item": "Rune pickaxe"
    },
    "target_clients": ["bot_1"]
}
```

#### Unequip Item
```json
{
    "action": "unequip_item",
    "parameters": {
        "item": "Rune pickaxe"
    },
    "target_clients": ["bot_1"]
}
```

### 4. Status Commands

#### Get Status
```json
{
    "action": "get_status",
    "parameters": {},
    "target_clients": ["bot_1"]
}
```

## How to Send Commands

### Using Python Requests

```python
import requests

# Send command to specific clients
def send_command(action, parameters, target_clients):
    url = "http://localhost:8080/api/commands/send"
    data = {
        "action": action,
        "parameters": parameters,
        "target_clients": target_clients
    }
    response = requests.post(url, json=data)
    return response.json()

# Examples:
# Walk bot_1 to Grand Exchange
send_command("walk_to_bank", {"bank": "GRAND_EXCHANGE"}, ["bot_1"])

# Make multiple bots deposit all items
send_command("deposit_all", {}, ["bot_1", "bot_2", "bot_3"])

# Walk all bots to specific coordinates
send_command("walk_to_coordinates", {"x": 3046, "y": 9776, "plane": 0}, ["bot_1", "bot_2"])
```

### Using cURL

```bash
# Walk to Grand Exchange
curl -X POST http://localhost:8080/api/commands/send \\
  -H "Content-Type: application/json" \\
  -d '{
    "action": "walk_to_bank",
    "parameters": {"bank": "GRAND_EXCHANGE"},
    "target_clients": ["bot_1"]
  }'

# Broadcast command to all active clients
curl -X POST http://localhost:8080/api/commands/broadcast \\
  -H "Content-Type: application/json" \\
  -d '{
    "action": "get_status",
    "parameters": {}
  }'
```

### Using JavaScript/Node.js

```javascript
const axios = require('axios');

async function sendCommand(action, parameters, targetClients) {
    try {
        const response = await axios.post('http://localhost:8080/api/commands/send', {
            action: action,
            parameters: parameters,
            target_clients: targetClients
        });
        console.log('Command sent:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error sending command:', error.response?.data || error.message);
    }
}

// Examples:
sendCommand('walk_to_bank', {bank: 'GRAND_EXCHANGE'}, ['bot_1']);
sendCommand('deposit_all', {}, ['bot_1', 'bot_2', 'bot_3']);
```

## Complex Command Sequences

### Mining Session Setup
```python
import requests
import time

def setup_mining_session(client_ids):
    base_url = "http://localhost:8080/api/commands/send"
    
    commands = [
        # 1. Walk to bank
        {"action": "walk_to_bank", "parameters": {"bank": "EDGEVILLE"}},
        # 2. Open bank
        {"action": "open_bank", "parameters": {}},
        # 3. Deposit all items
        {"action": "deposit_all", "parameters": {}},
        # 4. Withdraw pickaxe
        {"action": "withdraw_item", "parameters": {"item": "Rune pickaxe", "quantity": 1}},
        # 5. Close bank
        {"action": "close_bank", "parameters": {}},
        # 6. Equip pickaxe
        {"action": "equip_item", "parameters": {"item": "Rune pickaxe"}},
        # 7. Walk to mining location
        {"action": "walk_to_coordinates", "parameters": {"x": 3046, "y": 9776, "plane": 0}}
    ]
    
    for i, command in enumerate(commands):
        print(f"Sending command {i+1}/{len(commands)}: {command['action']}")
        
        response = requests.post(base_url, json={
            "action": command["action"],
            "parameters": command["parameters"],
            "target_clients": client_ids
        })
        
        print(f"Response: {response.json()}")
        time.sleep(2)  # Wait between commands

# Setup mining for bots 1-5
setup_mining_session(["bot_1", "bot_2", "bot_3", "bot_4", "bot_5"])
```

### Grand Exchange Trading
```python
def setup_ge_trading(client_ids, item_to_buy, quantity, max_price):
    commands = [
        {"action": "walk_to_bank", "parameters": {"bank": "GRAND_EXCHANGE"}},
        {"action": "open_bank", "parameters": {}},
        {"action": "withdraw_item", "parameters": {"item": "Coins", "quantity": max_price * quantity}},
        {"action": "close_bank", "parameters": {}},
        {"action": "walk_to_coordinates", "parameters": {"x": 3165, "y": 3486, "plane": 0}}  # GE booths
    ]
    
    for command in commands:
        send_command(command["action"], command["parameters"], client_ids)
        time.sleep(1)

setup_ge_trading(["bot_1", "bot_2"], "Dragon bones", 100, 3000)
```

## API Endpoints Reference

### Send Command to Specific Clients
- **POST** `/api/commands/send`
- Body: `{"action": "...", "parameters": {...}, "target_clients": [...]}`

### Broadcast Command to All Active Clients
- **POST** `/api/commands/broadcast`
- Body: `{"action": "...", "parameters": {...}}`

### Get All Connected Clients
- **GET** `/api/clients`

### Get Specific Client Status
- **GET** `/api/clients/{client_id}/status`

## Client Configuration

In your Microbot client, configure these settings:

1. **Server URL**: `http://your-server-ip:8080`
2. **Client ID**: Unique identifier (e.g., `bot_1`, `miner_01`, `ge_trader_1`)
3. **Poll Interval**: How often to check for commands (default: 1000ms)

## Error Handling

Commands will return error responses if:
- Required parameters are missing
- Client is not connected
- Invalid action specified
- Execution fails on client side

Example error response:
```json
{
    "error": "Client not found",
    "client_id": "bot_1",
    "command_id": "abc123"
}
```

## Best Practices

1. **Use descriptive client IDs**: `ge_trader_1`, `mining_bot_2`, `combat_main`
2. **Group similar tasks**: Send commands to groups of bots doing similar activities
3. **Add delays between commands**: Allow time for execution before sending next command
4. **Monitor status**: Check client status regularly to ensure commands are executing
5. **Handle errors**: Always check response status and handle failures appropriately

## Scaling to 100+ Clients

For large numbers of clients:

1. **Use client groups**: Group clients by activity type
2. **Batch commands**: Send commands to multiple clients at once
3. **Monitor performance**: Watch server response times
4. **Use databases**: Replace in-memory storage with Redis/PostgreSQL for production
5. **Load balancing**: Consider multiple server instances for high load

Example client grouping:
```python
client_groups = {
    "miners": ["miner_01", "miner_02", "miner_03"],
    "woodcutters": ["wc_01", "wc_02", "wc_03"],
    "fighters": ["combat_01", "combat_02"],
    "traders": ["ge_01", "ge_02"]
}

# Send mining command to all miners
send_command("walk_to_coordinates", {"x": 3046, "y": 9776}, client_groups["miners"])
```
'''

print("Command Examples and Usage Guide created!")
print("Length:", len(command_examples), "characters")

# Save the command examples
with open('CommandExamples.md', 'w') as f:
    f.write(command_examples)

print("File saved as CommandExamples.md")