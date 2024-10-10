# RuneLite Plugin Development Notes

## Current Ideas

### General
- (https://github.com/Truncuso/microbot-launcher-linux?tab=readme-ov-file)
- Add prehook for automated git repopack file, text file (consider using git lfs for it)
- MOCROSOFT — Today at 08:01
  - I changed the system to detect for a "i can't reach that!" message, once this is found, it triggers a pathing algorithm detecting the path that is interactable with the npc. Here is a video of what  I mean.
- projectiles ids? any db where i can found them
  - npcattackanimation.json
- AntiBan -
- 
  - reinvestigate the random functions in the antiban -> more  random anti ban "roboust" distributions also for click function ?
  - for move mouse off screen to make it with a percentage variable or introduce 2 more methods for it one that is 100% of the time and one that is 50% of the time
  - adjustInterval in the playstyle function should be random -> 
  - also check the random function and improve 
- Script functionalitie:
  - try to avoid getting object by name, cuz it needs to look into the osrs cache for it
    - for id it's already in memory and therefor a lot faster
  - should not block, ->  state machine like implementations 
  - should show the main state and add substate, when using other scripts for example or 
  - check antiban variables ->  actionCooldown or microbreak in progress ?
    - display in overlay
  - set the an
  - check if all scripts are pauses, FOR antiban - **Microbot.pauseAllScripts = true;** only set when universial antiban active, check actionCooldownActive for detection of action cooldwn in progress and microBreakActive for microbreak ->  pauseAllcripts also set when a break is active
    - ``` Java
      if (!Microbot.isLoggedIn()) return;
      if (!super.run()) return;
      if (Rs2AntibanSettings.actionCooldownActive) return;
    ```
  - always implentet he shutdown function-> destroy the sheudles object, stop the script
  - 
### RunLite based Microbot Plugins
- Note: RSplayer.nearestDistance ()->  shortpath plugin nearest distance calculation -> also teleports and doors....
#### Gauntelt Plugin:
- see squire bot for insperation -> state machine handling -> preperation phase and so on
- Vorkath's plugin or Scurrius pligonfor "dengoures object spwaning" ->  see also note in obsidtina note book for fast walking 
- implentation of a function  which finds nearst safe tiles (with paramter to set min distance away ) from current position ->  check deansogures object and also deangorues tile effects? -> use onObject
  - helpfull methods check  getNearestWalkableTileWithLineOfSight and , tileHasWalls, isValidTile  , getNearestWalkableTile or getSafeTile, in RS2Tile
- hanlde hunlef boss area ->  wolrd point area ? get coordinates
- rendering of objects and tiles -> microbotOverlay

#### Hallowed Sepulchre
- as insperation use the plugin from runlite ->  just copy it over, and handle interaction ?  marks all things correctly ?

#### QoL Navigation Plugin (VoxSylvaeNavigationPlugin)

- Serves as a plugin which can be used by other plugins
- User configuration options:
  - Add location from the scrapping of all locations via the wiki scrapper API
    - User can search for certain regions, POI, and/or sub-regions for navigation
    - Make menu for easy setup
  - Teleport categories to use when available:
    - Player owned house (PoH)
    - Spellbook
    - Tele tabs
    - Teleportation items (jewelry)
    - Fairy rings
    - Navigation over player owned house, try to navigate via teleportation options available in the player owned house
    - ... extend when more ideas come to mind
  - Configurable action hotkeys:
    - Click action to navigate to point on minimap -> clicking world map and holding action key
    - Click action to navigate to current active quest with active step -> clicking the current quest in the player quest tab
  - Toggle for minimap navigation use
- Standalone functionalities:
  - Navigate to world point by clicking on world minimap with hotkey pressed
    - Read in click world point location on world mini map
    - How can we get these via RuneLite and Microbot?
  - Find player's best teleportation options (load player's available teleportation points)
- Plugin should load and update current player teleportation options (current inventory, current skill level, magic, etc.) when a navigation function is executed which requires this information
- Use Microbot walker function for travel to world point and local points
  - For world navigation, minimap and canvas navigation should be mixed up when possible, and configured by the user
  - Simulate some anti-ban features, can we use antiban plugin and breakhandling plugin from Microbot for it?
- Check if quest helper plugin is available at startup and when a function needs it
- Check if player can use teleport -> required quest and magic level, have item in inventory or bank?
- Provide script (threaded scheduled execution) based functions to other plugins:
  - When the execution of such a function is finished, a flag should be set (navigationActionInProgress flag, private flag with getter only)
  - Commands only accepted if no other navigation action is in progress
  - Functions:
    - Navigate to certain area by name
      - Search world point coordinate from OSRS wiki, use scrapper tool utility
      - Use teleportation configured by the user and available > filter which teleportations are available
    - Navigate to current active quest step
      - Get player's current active quest, use quest helper plugin to determine current quest step, area to navigate to, or NPC to navigate to, or no navigation necessary -> Mqeuster already have this ?
      - Also use the scrapper for getting the world point coordinates
      - When we have to navigate to an NPC, do the RunLite objects have the saved world point?

### RunLite java based utility functions and scripts for plugin development


#### Scrapper Tool and information tool for getting Data From OSRS Wiki
- Use Wiki plugin from RuneLite?
- Implement search per string methods (try to remove typos)
- Improve name handling in the database, search string different from string in the database
- First implementation, rename the result class with -> WikiResult
  - Location data by name (VSMapScrapper -> rename?):
    - Get world point for an area by name
    - Make database for the navigation plugin
      - Categories -> added to the regions, subregions -> POI in the regions
  - Get information for items per item name or id (VSItemScrapper) -> methods for searching items per id:
    - Working with one version of an item in the info box
      - Example: https://oldschool.runescape.wiki/w/Abyssal_whip
      - Example multiple version https://oldschool.runescape.wiki/w/Abyssal_dagger
    - Item information:
      - Working first fetch "Infobox item" (extend for multiple versions):
        - ID
        - Name
        - Item tradeable
        - Item equippable
        - Item stackable
      - Work in progress:
        - get options ? use wear ... ? .>  string
        - get shop information
        - Get drop sources correct
        - Get item -> combat stats when equippable "Infobox Bonuses" -> https://oldschool.runescape.wiki/w/Abyssal_whip?action=edit&section=1
        - Item trading information (G.E. prices) -> only possible when have the "exchange" entry set to true
          - Live, extend the ItemWikiResult class by fetching methods, which using "VSwikiPriceScrapper" class
          - Add methods
          - API https://oldschool.runescape.wiki/w/RuneScape:Real-time_Prices
  - Get information for NPCs by NPC name or id (hostile ones are monsters and bosses) -> add search by id methods:
    - We have to distinguish between monster and NPC
    - Fetch multiple versions, add separate entry in the result:
      - IDs
      - Names
      - Locations
      - Slayer only?
      - Loot drops
      - Combat weaknesses
      - Other useful information?
- Extend Wiki Scrapper:
  - Merching Flipping Helper
  - Loot drop filters? -> by using loot data ->  extend on current impleneted loot filter object, generate loot filter per monster
  - Extend by finding loadout for certain activities, save the loadout for the min inventory manager:
    - In the future, by using some kind of LLM agent?:
      - Get best loadout for a certain activity (skilling or PvM)
      - Try to get best training method for skill and current level
      - Try to load money maker guides based on player unlocks and meeting skill requirements



#### Encapsulated Teleportation Data Database Object
- Read in JSON teleportation data on init
- Organize into:
  - Teleportation type/group (spellbook, player owned house options like portal nexus options, jewelry box option, spirit tree, fairy ring, items like skill capes/jewelry, fairy rings)
  - Player owned house options (like portal nexus options, jewelry box option, spirit tree, fairy ring)
    - Use location information loaded from the data JSON file
    - Jewelry box has jewelry in it which is also available as items
    - Nexus has options in it which are available as spells
    - ... fairy rings data and so on
  - Required items in inventory to perform teleportation:
    - e.g., fairy ring staff
    - Runes for magic spells
    - Items
    - For teleportation via player owned house, rune to player owned house
- Functions:
  - Find nearest teleport per group
  - For PoH option, get all available options
  - Find nearest teleport over all groups
  - Find nearest teleport over all groups considering player unlocks (getting a list of completed quests, and current skill levels?)
    - For PoH transportation methods, consider current construction level and if the player can have built the options
  - Find nearest teleport over all groups considering player unlocks (getting a list of completed quests, and current skill levels?), and list of items (items can be either in inventory or bank, not relevant for the database search and filtering)
  - ... any more ideas for getting/scrapping teleportation data from this class?
- Teleportation manager class helper, mainly for navigation:
  - Handle Fairy Rings. See Rs
  - Also interaction with item, check if in inventory and/or equipment. Handle differently?
  - Addition (actions in the JSON or how can we get action destination?)

#### Extend walker, navigation through the world helper plugin:
- Get last path from Rs2Walker -> pathfinder object via getter?
  - Also find or get transportation elements (fairy rings, doors, transportation) along the path?
  - How can I make a clean implementation, use the ecosystem of rs2walker or spawn a new thread in the VoxSylvaeNavigationScript which basically does the same as the rs2walker in the walkTo function
    - Check if target is already set:
      ```java
      if (currentTarget != null && currentTarget.equals(target) && ShortestPathPlugin.getMarker() != null && !Microbot.getClientThread().scheduledFuture.isDone())
          return false;
      setTarget(target);
      ```
    - ShortestPathPlugin.setReachedDistance(distance);
    - Spawn thread and get information:
      ```java
      Microbot.getClientThread().runOnSeperateThread(() -> {
          try {
              while (!Thread.currentThread().isInterrupted()) {
                  if (!Microbot.isLoggedIn()) {
                      setTarget(null);
                      break;
                  }
                  if (ShortestPathPlugin.getPathfinder() == null) {
                      if (ShortestPathPlugin.getMarker() == null)
                          break;
                      Microbot.status = "Waiting for pathfinder...";
                      continue;
                  }
                  if (!ShortestPathPlugin.getPathfinder().isDone()) {
                      Microbot.status = "Waiting for path calculation...";
                      continue;
                  }

                  if (isNear(ShortestPathPlugin.getPathfinder().getPath().get(ShortestPathPlugin.getPathfinder().getPath().size() - 1))) {
                      setTarget(null);
                      break;
                  }

                  //avoid tree attacking you in draynor
                  checkIfStuck();
                  if (stuckCount > 10) {
                      var moveableTiles = Rs2Tile.getReachableTilesFromTile(Rs2Player.getWorldLocation(), 5).keySet().toArray(new WorldPoint[0]);
                      walkMiniMap(moveableTiles[Random.random(0, moveableTiles.length)]);
                      sleep(600, 1000);
                  }

                  if (ShortestPathPlugin.getPathfinder() == null) break;

                  List<WorldPoint> path = ShortestPathPlugin.getPathfinder().getPath();
                  int indexOfStartPoint = getClosestTileIndex(path);
                  lastPosition = Rs2Player.getWorldLocation();
                  if (Rs2Player.getWorldLocation().distanceTo(target) == 0)
                      break;
                  //player holder,,,
              }
          } catch (Exception e) {
              e.printStackTrace();
          }
      });
      ```
    - Function which iterates over the path and:
      - Tracks doors, fairy rings and other transportation systems among the path like in handleDoors function
      - Calculates the path direction along the path -> moving direction of the player for each path point (for the last point in the path this is obviously zero)
        - see import net.runelite.api.coords.Direction; for directions ? like in RS2Tile
      - Tracks options for randomizing the path for each path point
      - New possible walkable tiles among the path, in the path direction
        - Track the new possible world points for the randomized possible tiles to walk
      - Restrict the randomization:
        - By the direction -> allow tile only when moving forward, also add a parameter to the function (number)
        - Near transportation objects, doors and fairy rings, path points on it, before and after these should not be randomized (min number before, min number after should be configurable)
      - Make an encapsulation of gathered data in one data object (make a new class for this purpose)
        - Add game objects and data (like doors, fairy rings (also fairy ring code needed?) or other useful for interaction which are necessary for the transportation) to the path index
        - For each path point, add the randomization options considering the given constraints
  - Or use setTarget from the rs2walker? --> get the pathfinder like in walkTo - Rs2walker.

##### Extend Rs2walker
- In ... interface with path and obstacles along the path
- Randomize the generated shortest path a bit. Only add new random tile if it is reachable, to this randomized walking directly in walkTo function of the RS2walker
- Make the walkTo function more modular
- Also calculate walking direction for the path, then based on this make the randomization, tiles only in front, left and right of the walking direction... always add reachable check for new tile which are considered

#### Extend in-game interaction:
- Allow right-click interaction?
- Click function with randomized pixel coordinates, by precalculation beforehand based on the click boxes of objects, NPCs, widgets, ... (convex hulls?)
  - Min max from original center, use fancy normal distribution, calculate per object/widget (when right-clicking) ... Calculate based on clickBox size... can we fetch these from the RuneLite object, widgets?
[Previous content remains the same]

#### Teleportation Manager Plugin
- Handles teleportations based on user settings
- Main user is the navigation plugin
- Functionality:
  - Handle Fairy Rings (See Rs implementation)
  - Interaction with items:
    - Check if in inventory and/or equipment
    - Handle differently based on location?
  - Addition: actions in the JSON or method to get action destination
- New cache structure for the use also in the navigation plugin ->  share cache:
  - Implement a more sophisticated path cache
  - Currently private to the teleport manager and only saving distance of a path
  - Enhance to cache the ShortestPathResult structure
  - Add getters for:
    - Start point
    - End point
    - Path distance
    - Path flow direction (vector of direction)
  - Implement path randomization

#### Navigation Plugin Enhancements
- Integrate with Teleportation Manager
- Implement new cache structure for improved path handling
- Path randomization:
  - Randomize the generated shortest path
  - Only add new random tile if there are reachable
  - Implement randomized walking directly in walkTo function of RS2Walker
- Make the walkTo function more modular
- Calculate walking direction for the path
- Based on walking direction, implement randomization:
  - Allow tiles only in front, left, and right of the walking direction
  - Always add reachable check for new tiles being considered
- Implementation details:
  - Use setTarget from RS2Walker
  - Get the pathfinder like in walkTo - RS2Walker
  - Spawn a new thread in VoxSylvaeNavigationScript that performs similar actions to RS2Walker's walkTo function
  - Check if target is already set before proceeding
  - Use ShortestPathPlugin for path calculations and distance settings
  - Implement stuck detection and handling (e.g., avoid tree attacking in Draynor)
  - Track doors, fairy rings, and other transportation systems along the path
  - Create a new class to encapsulate gathered data:
    - Add game objects and data (doors, fairy rings, transportation) to the path index
    - For each path point, add randomization options considering given constraints
  - Restrict randomization:
    - By direction (allow tiles only when moving forward)
    - Near transportation objects, doors, and fairy rings (configurable number of tiles before and after)


#### Monitoring Plugin
- Detect game state relevant data? thraed -> i dont think it is necessary ->most things found in utliity classes
  - 
  - Keep track of ground loot, NPCs, objects of interest... in such a range

### AIOHunterPlugin:
- Get area data for hunting locations (for each creature, numbers of available creatures)
- Get objects X tiles away from current player
- Split the hunting manager sub-state handler for actual hunting? Which also can be used separately when on the scale location. Implementation of a script? With run and a substructure
- Polymorphism abstract hunting creature script, lower scripts perform hunting based on the hunting style of the creature) scripts
- Abstract creatures should implement general hunting stuff, proper setup of scheduled hunting style script
- Hunting should be designed as a state machine, until target goal reached, ... or error occurred, or antiban making a break...
- Monitor creatures, traps and player (based on hunting creature)
- Create catch, lower scripts handle inventory management per hunting class (drop items, go to bank), performing trap placement calculation, tracking, detection of creature catches, detection of traps collapse, when necessary for the style
- Script per hunting style inheritance from hunting creature (falconry, tracking, monkial monkey (dead trading), box traps, net hunting?...)
- Each hunting style should provide necessary items (inventory, optimal equipment); based stuff, check before hunting, message box when not fulfilled... Go to finished state
- Hunting state machine --- separate thread (using antiban as set from the upper script) (AIOHunter)
- AIOHunter check if script finished
- Different creatures, different scripts?
  - simple implementation of chinchompa hunting script is found in the microbot->  only lays traps when frist lay down by the useer
- Wiki scrapper, use plugin
- Use it like the shortest path plugin in walker -> sheudlesexecution via thread ?
- Get actions for interaction with game objects and also add actions to items (bank id different?)

### Mixology Plugin
- Widget read like Wintertodt
  - found a enumum definiation allready for the different potions ?