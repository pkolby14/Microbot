# Microbot Utility Reference Map (MCP-Style Index)

---

## Game Data Reference

**This section provides authoritative sources and lookup instructions for all core game data used by Microbot utilities and plugins.**

### Item IDs
- **Source:** [`runelite-api/src/main/java/net/runelite/api/ItemID.java`](../runelite-api/src/main/java/net/runelite/api/ItemID.java)
- **Description:** Contains every item in the game, as auto-generated static integer fields. Example: `ItemID.EXCALIBUR = 35`.
- **Usage Pattern:**
  - In Java: `ItemID.EXCALIBUR` or `ItemID.BRONZE_ARROWTIPS`
  - In AI/MD: Lookup by name, then use the constant or integer value.
- **Sample Table:**

| Name                | ID  |
|---------------------|-----|
| DWARF_REMAINS       | 0   |
| TOOLKIT             | 1   |
| CANNONBALL          | 2   |
| ...                 | ... |

### Monster/NPC IDs & Stats
- **Source:** `util/npc/Rs2NpcManager.java` (loads from `/npc/monsters_complete.json` and related JSON files)
- **Description:** Provides all monster/NPC IDs, names, stats, and spawn locations. Used by AIO Fighter and all combat plugins.
- **Usage Pattern:**
  - To get stats: `Rs2NpcManager.getStats(npcId)`
  - To get locations: `Rs2NpcManager.getNpcLocations(npcName)`
  - To get closest spawn: `Rs2NpcManager.getClosestLocation(npcName)`
- **Sample Table:**

| Name        | ID    | HP | Attack | Defence | Locations (WorldPoint) |
|-------------|-------|----|--------|---------|-----------------------|
| Goblin      | 100   | 5  | 1      | 1       | (3245, 3248, 0), ...  |
| Cow         | 81    | 8  | 1      | 1       | (3256, 3266, 0), ...  |
| ...         | ...   |... | ...    | ...     | ...                   |

### Bank, Deposit Box, and Teleport Locations
- **Sources:**
  - Banks: `util/bank/enums/BankLocation.java`
  - Deposit Boxes: `util/depositbox/DepositBoxLocation.java`
  - Jewellery Teleports: `util/equipment/JewelleryLocationEnum.java`
- **Description:** Each enum contains name and `WorldPoint` coordinates for all major locations.
- **Usage Pattern:**
  - To get a bank location: `BankLocation.GRAND_EXCHANGE.getWorldPoint()`
  - To get a deposit box: `DepositBoxLocation.LUMBRIDGE.getWorldPoint()`
  - To get a teleport: `JewelleryLocationEnum.GRAND_EXCHANGE.getLocation()`
- **Sample Table (Banks):**

| Name                | WorldPoint (x, y, plane) | Members/Quest Req |
|---------------------|-------------------------|-------------------|
| GRAND_EXCHANGE      | (3166, 3485, 0)         | No                |
| EDGEVILLE           | (3094, 3492, 0)         | No                |
| ...                 | ...                     | ...               |

---

## Table of Contents
- [How to Use This File](#how-to-use-this-file)
- [Rs2Bank](#rs2bank)
- [Rs2Inventory](#rs2inventory)
- [Rs2Dialogue](#rs2dialogue)
- [Rs2GameObject](#rs2gameobject)
- [Rs2Player](#rs2player)
- [Rs2Walker](#rs2walker)

---

## How to Use This File
1. **Reference First:** When you need to implement, automate, or debug a feature, consult this map for the relevant class and method. Only search the codebase if the map does not provide sufficient detail.
2. **Navigation:** Each entry includes the file path, class summary, key methods (with signatures and 1-line summaries), related classes, and usage notes.
3. **Contribution:** Keep this file up to date. Add new classes, methods, and usage patterns as the codebase evolves.

---

## Rs2Bank (`util/bank/Rs2Bank.java`)
**Purpose:**
Automates bank interactions: open/close, deposit/withdraw, item locks, PIN, config save/load, and collection box.

**Key Methods:**
- `boolean openBank()` — Opens nearest bank (NPC/object/tile).
- `boolean closeBank()` — Closes the bank interface.
- `boolean depositAll(Predicate<Rs2ItemModel>)` — Deposits all items matching predicate.
- `Rs2ItemModel getBankItem(int id | String name)` — Finds a bank item by ID or name.
- `boolean toggleItemLock(Rs2ItemModel | String name, boolean exact)` — Locks/unlocks bank items.
- `boolean handleBankPin(String pin)` — Enters bank PIN if prompted.
- `void saveBankToConfig()` / `void loadBankFromConfig()` — Save/load bank data.

**Related:** Rs2Inventory, Rs2Widget, Rs2GameObject

**Usage Notes:**
- Bank interface must be open for most methods.
- Uses `sleepUntil` for UI state changes.
- Handles both manual and framework delays.

---

## Rs2Inventory (`util/inventory/Rs2Inventory.java`)
**Purpose:**
Manages inventory: item checks, interaction, slot access, using/destroying items, and inventory state.

**Key Methods:**
- `ItemContainer inventory()` — Returns inventory container.
- `boolean hasUnNotedItem(String name[, boolean exact])` — Checks for unnoted item.
- `boolean useLast(int id)` — Uses last item with ID.
- `boolean slotInteract(int slot, String action)` — Interacts with slot.
- `boolean interact(int id | String name | Rs2ItemModel, String action)` — Interacts with item.
- `Rs2ItemModel get(String name, boolean exact)` — Gets item by name.
- `int getFirstEmptySlot()` — First empty slot index.
- `void waitForInventoryChanges(Runnable action, int time, int timeout)` — Waits for inventory to change after action.

**Related:** Rs2Bank, Rs2Widget, Rs2GameObject

**Usage Notes:**
- Supports exact/partial name matching.
- Handles inventory widgets for bank/shop/GE contexts.

---

## Rs2Dialogue (`util/dialogues/Rs2Dialogue.java`)
**Purpose:**
Automates all in-game dialogues: NPC, player, options, cutscenes, and combinations.

**Key Methods:**
- `boolean isInDialogue()` — Returns true if any dialogue is active.
- `void clickContinue()` — Simulates space key for "Click here to continue".
- `boolean keyPressForDialogueOption(String text, boolean exact)` — Selects option by key press.
- `boolean clickCombinationOption(String text, boolean exact)` — Clicks combination option.
- `boolean hasDialogueOption(String text, boolean exact)` — Checks for dialogue option.
- `void waitForCutScene(int time, int timeout)` — Waits for cutscene.

**Related:** Rs2Widget, Rs2Keyboard

**Usage Notes:**
- Relies on widget checks, not ChatMessage events.
- Supports exact/partial text matching.
- Handles conditional UI delays.

---

## Rs2GameObject (`util/gameobject/Rs2GameObject.java`)
**Purpose:**
Finds/interacts with world objects (doors, chests, altars, etc.), including decorative/wall objects.

**Key Methods:**
- `boolean interact(GameObject | TileObject | int id | String name, [String action], [boolean exact], [int distance])` — Interacts with objects by type/ID/name/action.
- `boolean exists(int id)` — Checks if object exists.
- `boolean canReach(WorldPoint target, ... )` — Checks reachability.
- `List<TileObject> getAll([Predicate, distance, anchor])` — Finds objects by filter.

**Related:** Rs2Player, Rs2Tile, Rs2Inventory

**Usage Notes:**
- Flexible search by ID, name, anchor, or predicate.
- Supports local/world coordinates and reachability checks.

---

## Rs2Player (`util/player/Rs2Player.java`)
**Purpose:**
Accesses player state, skills, buffs, combat, run energy, and nearby players.

**Key Methods:**
- `boolean hasAntiFireActive()` — Checks anti-fire buff.
- `boolean hasStaminaBuffActive()` — Checks stamina buff.
- `int getCombatLevel()` — Gets combat level.
- `Stream<Player> getPlayers(Predicate, boolean includeLocal)` — Streams nearby players.
- `boolean logout()` — Logs out player.
- `Player getPlayer(String name[, boolean exact])` — Finds player by name.

**Related:** Rs2Inventory, Rs2GameObject, Rs2Bank

**Usage Notes:**
- Many methods run on client thread for safety.
- Deprecated methods are marked; use newer overloads.

---

## Rs2Walker (`util/walker/Rs2Walker.java`)
**Purpose:**
Handles all pathfinding, walking, and teleportation logic.

**Key Methods:**
- `boolean walkTo(WorldPoint | int x, int y, int plane[, int distance])` — Walks to world point.
- `WalkerState walkWithState(WorldPoint target, int distance)` — Walks with feedback state.
- `boolean walkNextTo(GameObject target)` — Walks next to object.
- `boolean processWalk(WorldPoint target, int distance)` — Core walk logic (doors, teleports, etc.).
- `void recalculatePath()` — Forces path recalculation.

**Related:** Rs2Player, Rs2GameObject, Rs2Bank

**Usage Notes:**
- Uses shortest-path plugin for advanced pathfinding.
- Supports walking and teleport-based movement.

---

## Core Utilities (Located in `util/`)

- **Movement & Pathfinding**
  - `walker/Rs2Walker.java` – Walking, pathfinding, walkTo, walkWithState, getWalkPath
  - `tile/` – Tile and coordinate helpers
- **Player Actions**
  - `player/Rs2Player.java` – Movement, run energy, eating, walkUnder, player info
- **Banking**
  - `bank/Rs2Bank.java` – Bank interactions, deposit, withdraw, open/close bank
- **Inventory**
  - `inventory/Rs2Inventory.java` – Inventory management, item checks, item usage
- **Dialogue**
  - `dialogues/Rs2Dialogue.java` – NPC and player dialogue handling
- **Game Objects**
  - `gameobject/Rs2GameObject.java` – Interact with world objects (doors, altars, etc.)
- **Combat**
  - `combat/` – Combat utilities, attacking, safespots, etc.
- **Mouse & Keyboard**
  - `mouse/` – Mouse movement, clicking, anti-ban
  - `keyboard/` – Keyboard input utilities
- **Magic & Prayer**
  - `magic/` – Spellcasting, teleports
  - `prayer/` – Prayer toggling and management
- **Other**
  - `antiban/` – Anti-ban and humanization utilities
  - `shop/`, `slayer/`, `woodcutting/`, etc. – Skill-specific helpers
  - `reflection/`, `events/`, `overlay/`, `settings/`, `math/`, `misc/`, etc.

## Major Plugins (Examples)

Each plugin typically contains:
- `*Script.java` – Main logic
- `*Plugin.java` – Plugin entry point
- `*Overlay.java` – UI overlays
- `*Config.java` – User configuration

**Examples:**
- **Barrows:** `barrows/BarrowsScript.java`, `barrows/BarrowsPlugin.java`, `barrows/BarrowsOverlay.java`, `barrows/BarrowsConfig.java`
- **Rev Killer:** `revKiller/revKillerScript.java`, `revKiller/revKillerPlugin.java`, etc.
- **Farm Tree Run:** `farmTreeRun/FarmTreeRunScript.java`, `farmTreeRun/FarmTreeRunPlugin.java`, etc.
- **Combat Hotkeys:** `combathotkeys/CombatHotkeysScript.java`, etc.
- **Example Script:** `example/ExampleScript.java`, `example/ExamplePlugin.java`, etc.
- *(...and so on for each plugin folder in your codebase)*

## Documentation & Overviews

- `MicrobotReferenceMap.md` (this file)
- `VaultLooter/VaultLooterOverview.md`
- `VoxPlugins/schedulable/example/README.md`
- `pluginscheduler/condition/location/readme.md`
- `pluginscheduler/condition/npc/ReadMe.md`
- `pluginscheduler/condition/resource/README.md`

*(Look for `*Overview.md` or `README.md` in plugin folders for detailed docs)*

## How to Use This Map

- **Need to walk to a tile?**  
  See `util/walker/Rs2Walker.java` – use `walkTo` or `walkWithState`
- **Need to bank?**  
  See `util/bank/Rs2Bank.java`
- **Need to check or use inventory?**  
  See `util/inventory/Rs2Inventory.java`
- **Need to handle dialogue?**  
  See `util/dialogues/Rs2Dialogue.java`
- **Want an example of a full script?**  
  See any `*Script.java` in a plugin folder (e.g., `barrows/BarrowsScript.java`)
- **Need anti-ban features?**  
  See `util/antiban/`
- **Skill-specific automation?**  
  Look for the relevant skill folder (e.g., `woodcutting/`, `slayer/`, `fishing/`, etc.)

---

This map can be expanded or refined as you add more plugins/utilities or as you want more detailed descriptions. If you want to automate updates or generate more detailed breakdowns, let me know!

test