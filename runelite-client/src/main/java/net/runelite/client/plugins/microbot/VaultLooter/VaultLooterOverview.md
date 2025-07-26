# VaultLooter Overview

## 1. Plugin Summary

**Core Purpose:** Automates the process of accessing and looting the Barbarian Assault reward vault by performing the required emote sequence and collecting items.

**Main Workflow:** 
1. Navigate to the vault entrance location (WorldPoint 3192, 9825, 0)
2. Execute the specific emote sequence required to unlock the vault
3. Enter the vault and collect available loot
4. Exit and repeat if configured

**Key Dependencies:** 
- Rs2Player (player location and status checking)
- Rs2Walker (pathfinding and movement)
- Rs2Widget (emote interface interaction)
- Rs2Tab (switching to emotes tab)

**Execution Model:** Continuous loop with 1-second intervals, checking player status and executing vault access sequence

## 2. File Stack Overview

**Script File (`VaultLooterScript.java`)**
- Main execution logic with scheduled task running every 1000ms
- Handles navigation to vault entrance
- Executes the 17-emote sequence required for vault access
- **Current Status:** Incomplete - missing actual looting logic

**Config File (`VaultLooterConfig.java`)**
- Currently empty interface extending Config
- **Planned Features:** Configuration options for loot preferences, delays, repeat settings

**Plugin File (`VaultLooterPlugin.java`)**
- Standard Microbot plugin lifecycle management
- Integrates overlay and script components
- Handles plugin startup/shutdown with overlay management

**Overlay File (`VaultLooterOverlay.java`)**
- Displays plugin status and version information
- Provides test button for popup functionality
- Shows current Microbot status in real-time

## 3. Detailed Code Breakdown

### Main Execution Loop (`run()` method)
**Java Implementation:** `scheduledExecutorService.scheduleWithFixedDelay()`
**Human Readable:** Creates a repeating task that executes every 1000 milliseconds to check player status and perform vault access sequence
**Framework Details:** Uses Microbot's built-in scheduler with 1-second intervals
**Dependencies:** Requires Microbot.isLoggedIn() and super.run() validation
**Current Issues:** No completion state - will repeat emotes indefinitely

### Navigation Logic
**Java Implementation:** `Rs2Walker.walkTo(new WorldPoint(3192, 9825, 0))`
**Human Readable:** Moves player to the vault entrance coordinates outside the Barbarian Assault minigame area
**Framework Details:** Uses Rs2Walker with built-in pathfinding and obstacle avoidance
**Dependencies:** Requires Rs2Player.isNearArea() for location validation
**Timing:** Uses sleepUntilTrue() to wait for arrival before proceeding

### Emote Sequence Execution
**Java Implementation:** `Rs2Tab.switchToEmotesTab()` followed by `Rs2Widget.clickWidget(emoteName)`
**Human Readable:** Opens the emotes interface and clicks each required emote in the specific sequence: Panic, No, Beckon, Laugh, Shrug, Cry, Spin, Yes, Think, Dance, Blow Kiss, Wave, Bow, Panic, Headbang, Jump for Joy, Angry
**Framework Details:** Each emote click has 600-1000ms random delay
**Dependencies:** Requires emotes tab to be accessible and widget names to match exactly
**Timing Issues:** Fixed delays may not account for animation completion

### Current Delay Structure
- **Tab Switch Delay:** 600-1000ms after switching to emotes tab
- **Emote Delays:** 600-1000ms between each emote execution (17 total emotes)
- **Total Sequence Time:** Approximately 18-34 seconds per complete sequence
- **Loop Interval:** 1000ms between execution checks

## 4. Update Log

### [2024-01-22 22:04] - Initial Version 1.0.0
- **Created:** Basic plugin structure with all four required files
- **Implemented:** Navigation to vault entrance at WorldPoint(3192, 9825, 0)
- **Implemented:** Complete 17-emote sequence for vault access
- **Added:** Basic overlay with status display and test button
- **Missing:** Actual vault looting logic after emote sequence
- **Missing:** Configuration options for customization
- **Issue:** Script repeats emote sequence continuously without completion state
- **Issue:** Hardcoded widget names may not be reliable across game updates
- **Timing:** Uses 600-1000ms delays between emotes, may need adjustment for stability

### Known Issues and Required Improvements
1. **Missing Loot Logic:** Script stops after emote sequence, needs vault entry and item collection
2. **No Completion State:** Script will repeat emotes indefinitely
3. **Widget Reliability:** Emote widget names may change, need more robust selection method
4. **Error Handling:** No validation for emote execution success
5. **Configuration:** Empty config file needs options for user customization
6. **Performance:** 1-second loop interval may be too frequent for this type of task
