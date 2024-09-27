## Current Ideas

### General
- add prehook for autometed git repopack file, text file (git lfs  for it ?)
### Runlite based Microbot Plugins
- QoL Navigation Plugin( VoxSylvaeNavigationPlugin) with serves as an plugin which can be used by other plugins
  - user can configure
    - teleports category used, when availabe
      - use Player owend house (PoH)
      - use spellbook
      - use tele tabs
      - use teleportation items (jewlery)
      - use fariy rings
      - ... when more come in me mind extent it
      - use navigation over player owned house, try to naviagte via options teleporations are avalibae via the player owned house
    - configure able action hot key 
      - click action can be navigate to point on minimap- >clicking world map and holding action key
      - click action can be navigate to current activa qeust with active step -> clicking the current qeust in the player qeust tab 
    - configure if minimap navigation should be used
    - stand alone functionlaties,
      -  navigate to world point by clicking on world minimap with hotkey pressed 
         -read in click world point location on wolrd mini map, 
        - how can we get these via runelite and mirco bot
      - find player best teleportation options (load players availbe teleporationpoitn)
        - if o      
  - plugin should load and update current player teleporation options(current inventory, current skill level. (magic,...))(when a naviagtion function is executed which req. these information)
  - plugin should use microbot walker function for travel to world point and local points, 
    - for wolrd naviagtion mini map and canvas navigation should be mixed up when possible, and configured by the user
    - simulate some anti ban features, can we use antiban plugin and breakhandling plugin from microbot for it ?
  - plugin should check if qeust helper plugin is avaliable a startup and when a function needs it
  - check if player can use tele port-> req. qeust and magic level, have item in inventors or bank?
  - provide script (threaded sudled execuation) based functions to other plugins,
    - when the execution of such a function is finsed a flag should be set (navigationActionInProgress flag private flag, getter only),
    - command only be accepet if not any other navigation action in progress 
    - functions
      - navigate to ceration area by name
        - search wolrd point coordinate from osrs wiki, use scrapper tool utilty
        - use teleporation configured by the user and availibe> filter which teleporation are avilaibe
      - navitate to current acitve qeust setep
        - get players current active qeust use qeust helper plugin to dertmine current qeust step, area to navigate to, or npc to naviagte to, or no navigation nesscary, also use the scrapper for getting the world point coorniates
          - when we have to naviagte to an npc, do the runlite object have the saved world point saved?
      - 
### Runlite java based utiltiy function and or scripts which can be used for plugin delvelopment:
#### Scarpper Tool and information tool for getting Data From osrs Wiki 
  -  use wikiplugin from runelite for it ?
  - in search per string methods (try to remove typos)
  - first implemntation
    - location data be name,
      - get world point for an area, 
    - get infromation for items  per item name or id
      - ids, 
      - names
      - item tradeable,
      - item trading information(g.e. prices)
    - get infromation for npcs by npc name or id (hostile on are monsters and bosses) 
      -  ids, 
      -  names
      -  locations
      -  slayer only ?
      -  loot drops, 
      -  combat weackness and 
      -  other usefull information ?
   - loot drop filters ?
  - in the future ,by using some kind of LLM agnet ?:
  - - get best loadout for a certain activy (skliing or PvM)
    - try to get best training metehod for skill and current level
    - try to load money maker guides based on player unlocks and meeting skill req.
#### encalsuplated Teleporation Data Databse Object 
  - read in json teleporation data on init
  - orgenize into 
    - teleporation type\group (spellbook, player owed house option (lile portal nexus options, jewely box opitoon, sprit tree ,. fariy fing), item(like skill capes)\jewleery,fariy rings)
    - player owed house option (lile portal nexus options, jewely box opitoon, sprit tree ,. fariy fing)
      - use location information loaded from the data json file,
        - jewlry box have jewelry in it which also are avilibe as items
        - nexus have options in it which are available as spells
        - ... fariy fings data.. and so on
    - req items in invnetory to perform teleporation 
      - e.g.airy ring staff ?, 
      - runes for magic spells
      -  item,..., 
      -  for teleporation via player owend house, rune to player owend house
  
  - find nearst teleport per group
  - for PoH option, get all aviable options
  - find nearest teleport over all groups
  - find nearest teleport over all groups considering player unlocks (getting a list of completed qeust?, and current skill levels ?)
    - for PoH trasnporation methods consider current construction level and if the player can have build the options
  - find nearest teleport over all groups considering player unlocks    ( getting a list of completed qeust?, and current skill levels ?), and list of items (items can be either in invnetor or bank not revlenat for the database serach and filtering)
  - ... any more ideas for getting\scrapping teleporation data from these class?
  - Teleportation manger class helper, for navigation manily
    - Handle Fairy Rings. See Rs
    - Also interaction with item, check if in inventory and or eq. Handlen differentty?
    Addition ( actions in the Json or how can we geh action destion?

#### Extend walker, navigation through the world helper plugin:
- Get last path from Rs2Walker ->  pathfinder object via getter ? 
  - Also find or get transportation elements( fairy rings, doors, transporation) along the path? 
  - How can I make a clean implementation, use the echo system of rs2walker or make a spwan a new thread in the VoxSylvaeNavigationScript which makes basily the same as the rs2wakler in the walkTo function
    -   check if target is allready set:
      ```if (currentTarget != null && currentTarget.equals(target) && ShortestPathPlugin.getMarker() != null && !Microbot.getClientThread().scheduledFuture.isDone())
            return false;
        setTarget(target);
      ```
      -    ShortestPathPlugin.setReachedDistance(distance);
      -     spwan treahd and get information: Microbot.getClientThread().runOnSeperateThread(() -> {
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
                          - function which iterates over the path  and the 
                          - track doors, fariy rings and other transporation system ammong the path like in handleDoors fucnton, 
                          - the function should alos calculate the path direction along the path ->  moving direction of the player for each path point (for the last point in the path these is obviouly zero), 
                          - also track options for randomizing the path for each path point
                          -  new possible walkable tiles among the path, in the path deriction 
                             -  track the new possible world points for the randomized possible tiles to walk 
                          -  -> restrict the randomisation 
                             -  by the direction-> allow tile only when we moving forward, also add a paramter to the function (number)
                             -  near transporation opjects, doors and fariy raings, path points on it, before and after these should not be randomized (min number before, min number after should be configurageble))
                          -  make a encapulsation gatherd data in one data object (make a new class for these pourpose),
                             -  add game objects and data (like doors, fariy rings (also fairy ring code need?) or other usefull for interaction which are nesscry for the transporation) to the the path index,
                             -  add for each path point add the randomizsation obptiosn considring the given constraions
                    ```
  - or use Set target from the rs2walker? --> get the pathfinder like in walkTO -Rs2walker.
##### Extent Rs2walker 
  - in ..  interface with path and obstacles along the path
- Randomise the generated shortest path a bit. Only add new random tile if it is reachable, to these randomised walking direlctiy in walkto function, of the RS2walker
- Make the walk to Funktion more modular.
- Also Calculate walking direction for the path, than based on these make the randomisation , tiles only in front, left and right of the walking dircretion..m always add reachable check for new tile which are considered
#### Extentend in game interaction:
- Right click interaction allow ?
- ... Click function with randomised pixel coordinates,by precalcution pefor hand based on the click boxes of objects, npcs, widgets, ...(convex hulls ?
    - Min max from orginnal Center , use fancy normal distribution, calculate per object/widget( when right clocking) ... Calculate based on clickBox size.. can elwe fetch these from the runelite object, widgets..



#### Monitoring Plugin 
- detecting  game state relavnt data ? Keep track of ground loot, npcs objects of interest.. in such a range
### AIOHunterPlugin:
-  Get area data for hunting locations, ( for each creature, @ numbers of available creatures
- Get object X tiles away from current player

Split the hunting manger sub state Handel for actual hunting ? Which also can be used separately when on the scale location. . Implementation of a script ? With run and a substructure
Polymorphism abstrakt hunting creature script, lower scripts pefrom hunting based on the Hunting style of the creature) scripts,
Abstract creatures should implement general hunting stuff, proper setup of schedule hunting style script)
Hunting should designed as a state machine, until target goal reached, ... Orr erro occurred, or antiban making a break...
monitor creatures, traps and player,( based on hunting creature),
xcreaute catch, lower scripts  handles inventory management per hunting class( drop items,... Go to bank ),  performing trap placement calculation, tracking, detection of creatur caughts, detection of traps collapse, when. Nesseary for the style.
Script per. hunting style inheritance from hunting creature ( falconey, tracking, moonkial monkey(dead trading), box traps, net hunting?..
each huntin style should provide nesseary items( inventory , optimal equipment); basesd stuff, check before hunting, messagbox when not fullied... Go to finished state
- hunting state machine--- seprete thread( using antiban as set from the upper script)(aiohunter)
Aio hunter check if script finished

Different creatures different script's?

Wiki scrapper, use plugin
Use it like the shotes path plugin in walker
Get actions for interaction with game objects ant also add actions to items( bank id different?)

Mix ology Plugin? Widget read like wintertodt

# RuneLite Plugin Development Notes

## Current Ideas

### General
- Add prehook for automated git repopack file, text file (consider using git lfs for it)

### RunLite based Microbot Plugins

#### QoL Navigation Plugin (VoxSylvaeNavigationPlugin)
- Serves as a plugin which can be used by other plugins
- User configuration options:
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
      - Get player's current active quest, use quest helper plugin to determine current quest step, area to navigate to, or NPC to navigate to, or no navigation necessary
      - Also use the scrapper for getting the world point coordinates
      - When we have to navigate to an NPC, do the RunLite objects have the saved world point?

### RunLite java based utility functions and scripts for plugin development

#### Scrapper Tool and information tool for getting Data From OSRS Wiki
- Use Wiki plugin from RuneLite?
- Implement search per string methods (try to remove typos)
- First implementation:
  - Location data by name:
    - Get world point for an area
  - Get information for items per item name or id:
    - IDs
    - Names
    - Item tradeability
    - Item trading information (G.E. prices)
  - Get information for NPCs by NPC name or id (hostile ones are monsters and bosses):
    - IDs
    - Names
    - Locations
    - Slayer only?
    - Loot drops
    - Combat weaknesses
    - Other useful information?
  - Loot drop filters?
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

#### Monitoring Plugin
- Detect game state relevant data? Keep track of ground loot, NPCs, objects of interest... in such a range

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
- Wiki scrapper, use plugin
- Use it like the shortest path plugin in walker
- Get actions for interaction with game objects and also add actions to items (bank id different?)

Mixology Plugin? Widget read like Wintertodt