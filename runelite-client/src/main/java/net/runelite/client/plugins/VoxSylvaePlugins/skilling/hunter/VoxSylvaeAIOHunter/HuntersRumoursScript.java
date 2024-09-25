package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;

import net.runelite.api.Skill;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter.AIOHunterConfig.HuntingMode;
import net.runelite.client.plugins.VoxSylvaePlugins.util.*;
import net.runelite.client.plugins.VoxSylvaePlugins.util.navigation.*;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.wintertodt.enums.State;
import net.runelite.client.plugins.questhelper.runeliteobjects.extendedruneliteobjects.FaceAnimationIDs;

public class HuntersRumoursScript extends Script {

    public static String version = "0.0.1";
    public static int timeout = 0;
    
    private HunterCreatureTarget currentHuntingCreatureTarget = null;
    private AIOHunterConfig config;
    private AIOHunterTaskManager rumoursTaskManager;
    @Inject
    private VoxSylvaeInventoryAndBankManagementScript inventoryAndBankManagementScript;
    @Inject
    private VoxSylvaeNavigationScript navigationScript;
    /*public enum HunterState {
        CHECK_REQUIREMENTS,   // Check if the player meets the requirements for the task
        SELECT_TASK_MASTER,   // Select the task master (configured or default)
        GET_TASK,             // Get a new task from the task master
        NAVIGATE_TO_HUNT,     // Navigate to the hunting area
        PERFORM_HUNTING,      // Perform the hunting task
        CHECK_BREAK,          // Check if a break should be taken
        GET_NEW_TASK,         // After hunting, get a new task
        FINISHED              // Bot is done with the task or has finished
    }*/
    public enum HunterState {
        INITIALIZE,   // Initialize the bot
        CHECK_REQUIREMENTS,   // Check if the player meets the requirements for the task
        SELECT_TASK,   // Select the task master (configured or default)
        GET_TASK,             // Get a new task from the task master
        SETUP_INVENTORY,      // Setup the inventory for the task
        NAVIGATE_TO_HUNT,     // Navigate to the hunting area
        PERFORM_HUNTING,      // Perform the hunting task
        PERFORM_SNARE,        // Perform the snare hunting task
        CHECK_BREAK,          // Check if a break should be taken
        
        FINISHED              // Bot is done with the task or has finished
    }

    private HunterState currentState = HunterState.INITIALIZE;
    public static boolean resetActions = false;
    private static boolean lockState = false;
    public HunterState getCurrentState() {
        return currentState;
    }
    private void initialize(AIOHunterConfig config) {
        
        int hunterLevel = getSkillLevel(Skill.HUNTER);
        /*HunterTask currentTask = rumoursTaskManager.getRumourTaskForLevel(hunterLevel);*/
        //rumoursTaskManager = new HunterRumoursTaskManager();
        //classicHuntingManager = new ClassicHuntingManager();
        this.config = config;
        currentState = HunterState.CHECK_REQUIREMENTS;
        inventoryAndBankManagementScript = new VoxSylvaeInventoryAndBankManagementScript();
        navigationScript = new VoxSylvaeNavigationScript();
        loadNpcData();
    }

     private static void changeState(HunterState scriptState) {
        changeState(scriptState, false);
    }

    private static void changeState(HunterState scriptState, boolean lock) {
        if (state == scriptState || lockState) return;
        System.out.println("Changing current script state from: " + state + " to " + scriptState);
        state = scriptState;
        resetActions = true;
        setLockState(scriptState, lock);
        lockState = lock;
    }

    private static void setLockState(HunterState state, boolean lock) {
        if (lockState == lock) return;
        lockState = lock;
        System.out.println("State " + state.toString() + " has set lockState to " + lockState);
    }

    public boolean run(AIOHunterConfig config) {        
        Rs2Antiban.resetAntibanSettings();
        //Rs2Antiban.antibanSetupTemplates.applyHunterSetup();
        applyAntiBanSettings();
        Rs2Antiban.setActivity(Activity.GENERAL_HUNTER);
        
        
        //int unprocessedItemPrice = Microbot.getItemManager().search(config.ITEM().getName()).get(0).getPrice();
        //int processedItemPrice = Microbot.getItemManager().search(config.ITEM().getFinished()).get(0).getPrice();
        //profitPerPlank = processedItemPrice - unprocessedItemPrice;

        //useSetDelay = config.useSetDelay();
        //setDelay = config.setDelay();
        //useRandomDelay = config.useRandomDelay();
        //maxRandomDelay = config.maxRandomDelay();     

        initialize(config);
        
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
          try {
                if (!Microbot.isLoggedIn()) return;
                if (Microbot.pauseAllScripts) return;
                if (!super.run()) return;
                if (Rs2AntibanSettings.actionCooldownActive) return;
                /*if (startCourse == null) {
                    Microbot.showMessage("Agility course: " + config.agilityCourse().name() + " is not supported.");
                    sleep(10000);
                    return;
                }*/
                long startTime = System.currentTimeMillis();
                
                final LocalPoint playerLocation = Microbot.getClient().getLocalPlayer().getLocalLocation();
                final WorldPoint playerWorldLocation = Microbot.getClient().getLocalPlayer().getWorldLocation();
                
                handleState();
            
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            


        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    public void onGameTick() {
      
    }
    
 
    public void onChatMessage(ChatMessage event) {
    
    }
    public void onProfileChanged(ProfileChanged event) {
    }
    private int getSkillLevel(Skill skill) {
        return Microbot.getClient().getRealSkillLevel(skill);
    }

    @Override
    public void shutdown() {
        Rs2Antiban.resetAntibanSettings();
        super.shutdown();
        Microbot.pauseAllScripts = true;
    }
    private void loadNpcData() {
        try {
            Rs2NpcManager.loadJson();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load NPC data", e);
        }
    }
    /*private void loadFoodItems() {
        try {
            List<Rs2Item> foods = Microbot.getClientThread().runOnClientThread(Rs2Inventory::getInventoryFood);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load item data", e);
        }
    }*/

    private void handleState() {

        if (!super.run() || !Microbot.isLoggedIn() || Rs2Antiban.isIdleTooLong(500)) {
            //oreVein = null;
            //miningSpot = MLMMiningSpot.IDLE;
            currentState = HunterState.INITIALIZE;
            return;
        }

        //if (pickAxeInInventory.isEmpty() && config.pickAxeInInventory()) {
            //Microbot.showMessage("Pickaxe was not found in your inventory");
        //    sleep(5000);
        //    return;
        //}

        if (Rs2AntibanSettings.actionCooldownActive) return;

        if (Rs2Player.isAnimating() || Microbot.getClient().getLocalPlayer().isInteracting()) return;

        switch (currentState) {
            case INITIALIZE:
                Rs2Antiban.setActivityIntensity(Rs2Antiban.getActivity().getActivityIntensity());
                initializeHunting();
                break;
            case SELECT_TASK:
                Rs2Antiban.setActivityIntensity(Rs2Antiban.getActivity().getActivityIntensity());
                selectNewHunterTask();
               
                break;
            case GET_TASK:
                Rs2Antiban.setActivityIntensity(Rs2Antiban.getActivity().getActivityIntensity());
                getNewHunterTask();
                break;
            case SETUP_INVENTORY:
                Rs2Antiban.setActivityIntensity(Rs2Antiban.getActivity().getActivityIntensity());
                loadInventorySetup();
                
                break;
            case CHECK_REQUIREMENTS:
                //
                if (checkHunterRequirements()) {
                    currentState = HunterState.NAVIGATE_TO_HUNT;
                } else {
                    currentState = HunterState.FINISHED;
                }
                break;
            
            case NAVIGATE_TO_HUNT:
            
                if (navigateToHuntingArea()) {
                    currentState = HunterState.PERFORM_HUNTING;
                }
                break;
            case PERFORM_HUNTING:
                if (currentHuntingCreature == null) {
                    currentState = HunterState.SELECT_TASK;

                    return;
                }
                //any chinchompa: black, red, grey
                if (currentHuntingCreatureTarget.getName().toLowerCase() == "Black chinchompa".toLowerCase()
                    || currentHuntingCreatureTarget.getName().toLowerCase() == "Grey chinchompa".toLowerCase()
                    || currentHuntingCreatureTarget.getName().toLowerCase() == "Red chinchompa".toLowerCase()) {
                    Rs2Antiban.setActivity(Activity.HUNTING_BLACK_CHINCHOMPAS);                                        
                }elif (currentHuntingCreatureTarget.getName().toLowerCase() == "Moonlight Antelope".toLowerCase()) {
                    Rs2Antiban.setActivity(Activity.HUNTING_BLACK_CHINCHOMPAS);                                        
                }elif (currentHuntingCreatureTarget.getName().toLowerCase() == "Sunligth Antelope".toLowerCase()) {
                    Rs2Antiban.setActivity(Activity.HUNTING_SUNLIGHT_ANTELOPES); 
                }elif (currentHuntingCreatureTarget.getName() == "Herbiboar") {
                    
                    Rs2Antiban.setActivity(Activity.HUNTING_HERBIBOARS);    
                }elif (currentHuntingCreatureTarget.getName() == "Moonlight Moth") {
                    
                    Rs2Antiban.setActivityIntensity(ActivityIntensity.EXTREME);    
                    
                }else{
                    Rs2Antiban.setActivity(Activity.GENERAL_HUNTER);
                }
                
                
                performHunting(this.currentHuntingCreatureTarget);
                break;
            case CHECK_BREAK:
                break;
            case FINISHED:
                shutdown();
                break;
        }
    }
    private void loadInventorySetup(){
        String inventorySetupName = "";//placeholder, get name from currentHuntingCreature
        if (!inventoryAndBankManagementScript.loadInventoryAndEquipment(inventorySetupName)){
            currentState = HunterState.FINISHED;
            Microbot.showMessage("Failed to load inventory setup");
        }else{
            Microbot.log(version + "<loadInventorySetup> loaded inventory setup: " + inventorySetupName+ " for hunter task: " + currentHuntingCreatureTarget.getName()+ "completed at: " +System.currentTimeMillis());
            currentState = HunterState.CHECK_REQUIREMENTS;
        }
          

    }
    private void selectNewHunterTask(){

        
        if (config.huntingMode() == HuntingMode.CLASSIC_HUNTING ) {
            //rumoursTaskManager.initialize();
            currentHuntingCreatureTarget = config.preferredHuntingCreature();
        } else {
            currentHuntingCreatureTarget = null;
            //classicHuntingManager.initialize(config.classicHuntingCreature());
        }
        currentState = HunterState.GET_TASK;
        

    }
    private void getNewHunterTask() {
        //getnew hunter task when we do hunter rounmours
        if (currentHuntingCreatureTarget == null) {
            //get possible task master(highst for the current level)
            //navigate to task master
            //get task by interaction with the task master npc
            //chat box, check which tast we have gotten
            //set currentHuntingCreatureTarget to the task we have gotten
            //when successful, set currentState = HunterState.SETUP_INVENTORY;
            //otherwise wait for the until we have been there,, when some thing fails, set currentState = HunterState.FINISHED;
            // currentState = HunterState.SETUP_INVENTORY;
            return;
        }
        
        currentState = HunterState.SETUP_INVENTORY;
        // Walk to the task master, only one step at a time
        //Microbot.getWalker().walkTo(master.getLocation());
    
        // Interact with the task master to get a new task (performed over multiple ticks)
        //interactWithTaskMaster(master);
        
    }
    private void initializeHunting() {
        
        
    }

    private WallObject findClosestVein() {
        return Rs2GameObject.getWallObjects().stream()
                .filter(this::isVein).filter(this::isWithinMiningArea).min((a, b) -> Integer.compare(distanceToPlayer(a), distanceToPlayer(b))).orElse(null);
    }

    private boolean isVein(WallObject wallObject) {
        int id = wallObject.getId();
        return id == 26661 || id == 26662 || id == 26663 || id == 26664;
    }
 
    private boolean isWithinMiningArea(WallObject wallObject) {
        WorldArea WEST_UPPER_AREA = new WorldArea(3748, 5676, 7, 9, 0);
        WorldArea EAST_UPPER_AREA = new WorldArea(3755, 5668, 8, 8, 0);
        boolean mineUpstairs = true;
        if (!mineUpstairs)
            return true;
        WorldPoint walkableTile = wallObject.getWorldLocation();
        return WEST_UPPER_AREA.contains(walkableTile) || EAST_UPPER_AREA.contains(walkableTile);
    }

    private int distanceToPlayer(WallObject wallObject) {
        WorldPoint closestWalkableNeighbour = Rs2Tile.getNearestWalkableTile(wallObject.getWorldLocation());
        if (closestWalkableNeighbour == null) return 999;
        return Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo2D(closestWalkableNeighbour);
    }
    private void performHunting(HunterCreatureTarget creatureTarget) {
        
        //check which hunting acction must be peformed, based on the current creature target
        //only start with implentation of snare hunting         
        // when we have to setup traps, find optimal locations
        // based on the current number of creatures near by number of available traps, check if the location is not occupied by game obejects(like trees, rocks, etc)-> valid tile for placeing a trap
        // for deadfall hunting only only location ->  stay at the location, check if the trap is setup, if not set it up
        // pidfall, find all near by pitfalls, check if they are setup, other player near by, switch world, occupy the location
        /*LootingParameters itemLootParams = new LootingParameters(
                                config.distanceToStray(),
                                1,
                                1,
                                config.minFreeSlots(),
                                config.toggleDelayedLooting(),
                                config.toggleLootMyItemsOnly(),
                                config.listOfItemsToLoot().split(",")
                        );
        */
        /*if (Rs2GroundItem.lootItemsBasedOnNames(itemLootParams)) {
            Microbot.pauseAllScripts = false;
            Rs2Antiban.actionCooldown();
            Rs2Antiban.takeMicroBreakByChance();
        }*/
       /* LootingParameters valueParams = new LootingParameters(
            config.minPriceOfItem(),
            config.maxPriceOfItem(),
            config.distanceToStray(),
            1,
            config.minFreeSlots(),
            config.toggleDelayedLooting(),
            config.toggleLootMyItemsOnly()
        );
        if (Rs2GroundItem.lootItemBasedOnValue(valueParams)) {
            Microbot.pauseAllScripts = false;
            Rs2Antiban.actionCooldown();
            Rs2Antiban.takeMicroBreakByChance();
        }
            Rs2Antiban.takeMicroBreakByChance();
        } */
    private void performBox(HunterCreatureTarget creatureTarget) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location

        Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
    }
    private void performHerbiboarHunting(HunterCreatureTarget creatureTarget) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location

        Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
    }
    private void performBirdHunting(HunterCreatureTarget creatureTarget) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location

        Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
    }
    private void performButterflyHunting(HunterCreatureTarget creatureTarget) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location

        Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
    }
    private void performDeadfall(HunterCreatureTarget creatureTarget) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location

        Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
    }
    private void performNet(HunterCreatureTarget creatureTarget) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location

        Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
    }
    private void performPitfall(HunterCreatureTarget creatureTarget) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location

        Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
    }
    private void performSnare(HunterCreatureTarget creatureTarget) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location

        Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
    }
    private void performTracking(HunterCreatureTarget creatureTarget) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location

        Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
    }
    private void findAllInteractableTraps(){
        //find all traps that are setup and ready to catch an animal
        //huntingplugin must have these logic
    }
    private void checkTrap(String trapName){
        //check if trap is setup and if it has caught an animal
        //huntingplugin must have these logic
    }
    private void setupTrap(String trapName){
        //setup trap, place it in the correct location, move loc to the correct tile, place down the trap, wait animation to finish with sleepunitl
    }
    private boolean checkHunterRequirements() {
        int hunterLevel = getSkillLevel(Skill.HUNTER);
        return true;
    }
    private boolean navigateToHuntingArea() {
        WorldPoint destination = null;
        if (currentHuntingCreatureTarget != null) {
            List<CreatureLocation> locations = currentHuntingCreatureTarget.getLocations();
            if (locations != null && !locations.isEmpty()) {
                destination = locations.get(0).getWorldPoint();
            }
            if (!navigationScript.walkTo(destination, 1)){
                Microbot.showMessage("Failed to navigate to hunting area");
                currentState= HunterState.FINISHED;
            }

        }
        //Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
        return false;
    }
    private void applyAntiBanSettings() {
        Rs2AntibanSettings.antibanEnabled = true;
        Rs2AntibanSettings.usePlayStyle = true;
        Rs2AntibanSettings.simulateFatigue = true;
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.behavioralVariability = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.naturalMouse = true;
        Rs2AntibanSettings.moveMouseOffScreen = true;
        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.dynamicIntensity = true;
        Rs2AntibanSettings.devDebug = false;
        Rs2AntibanSettings.moveMouseRandomly = true;
        Rs2AntibanSettings.takeMicroBreaks = true;
        Rs2AntibanSettings.microBreakDurationLow = 3;
        Rs2AntibanSettings.microBreakDurationHigh = 15;
        Rs2AntibanSettings.actionCooldownChance = 0.4;
        Rs2AntibanSettings.microBreakChance = 0.15;
        Rs2AntibanSettings.moveMouseRandomlyChance = 0.1;
    }
   

}