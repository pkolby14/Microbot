# Microbot Central Command Bridge - Testing Workflow

## 🚀 Quick Start Guide - 3 Simple Steps

### Step 1: Backend Setup (2 minutes)
```bash
cd central-command-backend/src/main/java
javac CentralCommandServer.java
java CentralCommandServer
```
**Port 8080 will start automatically**

### Step 2: Plugin Build (3 minutes)
```bash
cd runelite-client/src/main/java/net/runelite/client/plugins/microbot
javac CentralCommandBridge.java
```
**No compilation needed - uses existing Microbot structure**

### Step 3: Dashboard Launch (1 minute)
```bash
cd dashboard/src/main/java
javac BotDashboard.java
java BotDashboard
```
**WebSocket auto-connects to port 8081**

## 🔧 Testing Commands - Ready-to-Use Examples

### Test 1: Single Bot Walk Command
```json
{
  "action": "walk",
  "x": 3245,
  "y": 3248,
  "z": 0,
  "bots": ["bot1"]
}
```
**Uses existing Rs2Walker.walkTo() method**

### Test 2: Green Dragon Attack
```json
{
  "action": "attack",
  "npc": "Green Dragon",
  "time": 60,
  "bots": ["bot1", "bot2"]
}
```
**Uses existing Rs2Npc.interact() method**

### Test 3: Banking Operation
```json
{
  "action": "bank",
  "operation": "deposit",
  "bots": ["bot1"]
}
```
**Uses existing Rs2Bank.depositAll() method**

## 📋 No-Build Testing - Direct Method Calls

### Method 1: Direct Microbot API Test
```java
// Test existing methods directly
Rs2Walker.walkTo(new WorldPoint(3245, 3248, 0));
Rs2Npc.interact("Green Dragon", "Attack");
Rs2Bank.openBank();
```

### Method 2: Command Dispatch Test
```java
// Test command dispatch system
CentralCommandBridge bridge = new CentralCommandBridge();
bridge.dispatchCommand("walk", location);
```

## 🎯 Testing Checklist - 5-Minute Setup

□ **Backend running** - port 8080 active
□ **Plugin loaded** - CentralCommandBridge active
□ **Dashboard open** - BotDashboard visible
□ **WebSocket connected** - real-time updates
□ **Command dispatched** - test walk/attack/bank

## 📊 Real-Time Monitoring

**Dashboard displays:**
- Connected bot count
- Bot locations/status
- Command execution status
- Real-time WebSocket updates

## 🔧 Troubleshooting

**Port conflicts:** Change ports in CentralCommandServer.java
**Connection issues:** Check WebSocket URL ws://localhost:8081/bots
**Bot registration:** Bots auto-register on startup

## 🚀 Ready-to-Test Commands

**No compilation framework needed** - uses existing Microbot methods:
```json
{
  "action": "walk",
  "location": {"x":3245,"y":3248,"z":0},
  "bots": ["bot1"]
}
```
**Test immediately** - uses Rs2Walker.walkTo() existing method
