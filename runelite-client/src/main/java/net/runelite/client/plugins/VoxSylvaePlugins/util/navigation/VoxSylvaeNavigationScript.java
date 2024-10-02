package net.runelite.client.plugins.VoxSylvaePlugins.util.navigation;
import net.runelite.client.plugins.VoxSylvaePlugins.util.*;
import net.runelite.client.plugins.VoxSylvaePlugins.util.teleport.TeleportationManager;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.VoxSylvaePlugins.util.teleport.Teleport;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.vorkath.VorkathScript;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.crafting.enums.BoltTips;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;


import javax.inject.Inject;

import lombok.Getter;

import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Random;


import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import static net.runelite.client.plugins.microbot.util.Global.sleep;
import net.runelite.api.*;
import java.util.stream.Collectors;
public class VoxSylvaeNavigationScript extends Script {
    public enum NavigationState {
        IDLE,
        WAITING_FOR_PATH_FINDING,
        WALKING,
        WAITING_FOR_TELEPORTAION,
        TELEPORTING,        
    }
    @Inject
    private VoxSylvaeInventoryAndBankManagementScript inventoryAndBankManagementScript;
    @Inject
    private Client client;
    private static TeleportationManager teleportationManager;
    private static Map<String, Teleport> availableTeleports = new HashMap<>();
    private static final WorldPoint BANK_LOCATION = new WorldPoint(3183, 3436, 0); // Example: Varrock West Bank
    private NavigationState navigationState = NavigationState.IDLE;
    @Getter
    private String statusLast = "...";
    private WorldPoint currentDesiredLocation = null;
    private VoxSylvaeNavigationConfig config;
    public String getNavigationStatus() {
       return statusLast; 
    }
    public NavigationState getNavigationState() {
        return navigationState;
    }
    public WorldPoint getCurrentDesiredLocation() {
        return currentDesiredLocation;
    }
    private void initialize() {
        //add cache for path, init teleporatiom manager
        teleportationManager = new TeleportationManager(client);
        loadAllTeleports();
        checkPlayerTeleports();
    }

    private void loadAllTeleports() {
        

        
    }
    public boolean run(Client client, VoxSylvaeNavigationConfig config) {
        Microbot.enableAutoRunOn = true;
        Microbot.pauseAllScripts = false;
        
        //init = true;
        //state = State.BANKING;
        //hasEquipment = false;
        //hasInventory = false;
        //VorkathScript.config = config;
        //tempVorkathKills = config.SellItemsAtXKills();
        Microbot.getSpecialAttackConfigs().setSpecialAttack(true);
        
        this.config = config;
        if (client == null) {
          this.client = Microbot.getClient();
        }else{
          this.client = client;
        }        
        
        initialize();
       
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
             
                
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    
    
    private void checkPlayerTeleports() {
        int magicLevel = getSkillLevel(Skill.MAGIC);
        for (Map.Entry<String, Teleport> entry : availableTeleports.entrySet()) {
            Teleport teleportEntry = entry.getValue();
            if (magicLevel >= teleportEntry.getRequiredLevel() && inventoryAndBankManagementScript.hasAllItemsInInventory(teleportEntry.getRequiredItems())) {
                //info.isAvailable = true;
            }
        }
    }

    /*public boolean teleportToLocation(String teleportName, WorldPoint finalDestination) {
        TeleportInfo teleport = availableTeleports.get(teleportName);
        if (teleport != null && teleport.isAvailable) {
            Rs2Magic.castSpell(teleport.spell);
            sleep(3000, 4000);
            
            if (!Microbot.getClient().getLocalPlayer().getWorldLocation().equals(finalDestination)) {
                return walkTo(finalDestination,2);
            }
            return true;
        } else {
            System.out.println("Teleport not available: " + teleportName);
            return walkTo(finalDestination,2);
        }
    }*/
  
    private boolean navigateToWithTeleport(WorldPoint destination, int distance) {
        // find the nearest teleport to the destination
        Teleport nearestTeleport = teleportationManager.findNearestTeleport(destination, true);
        
        if (Rs2Walker.getDistanceBetween(Rs2Player.getWorldLocation(), destination) > distance) {
            return navigateTo(destination, distance);
        } else {
            return true;
        }
    }
    public boolean navigateTo(WorldPoint destination, int distance) {
        this.navigationState = NavigationState.WAITING_FOR_PATH_FINDING;
        this.currentDesiredLocation = destination;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                while (this.navigationState != NavigationState.IDLE) {
                    
                
                
                    //Rs2Walker.walkTo(NORTH_OF_WEB, 2);
                    //return sleepUntil(() -> Rs2Walker.getDistanceBetween(playerLocation, NORTH_OF_WEB) < 5 && !Rs2Player.isMoving(), 300);
                    if (ShortestPathPlugin.getPathfinder() == null) {
                        if (ShortestPathPlugin.getMarker() == null)
                            break;
                            statusLast = "[VS Navigation] Waiting for pathfinder ready...";
                        continue;
                    }
                    if (!ShortestPathPlugin.getPathfinder().isDone()) {
                        statusLast = "[VS Navigation] Waiting for path calculation...";
                        continue;
                    }
                    Microbot.log("Start Walk to destination. " + "x:" + destination.getX() + "y:" + destination.getY() + "distance:" + distance);
                    this.navigationState = NavigationState.WALKING;
                    sleepUntil(() -> Rs2Walker.walkTo(destination, distance) && !Rs2Player.isMoving());
                    this.navigationState = NavigationState.IDLE;
                }
            } catch (Exception ex) {
                this.navigationState = NavigationState.IDLE;
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        //sleepUntil(() -> Rs2Walker.walkTo(destination, distance) && !Rs2Player.isMoving(), 300);
        
        return true;
    }
    public boolean navigateAndOpenBank ( WorldPoint desiredBankLocation) {
        if (Rs2Player.getWorldLocation().equals(desiredBankLocation)) {
            
            return inventoryAndBankManagementScript.openNearestBank();
        } else {
            navigateTo(desiredBankLocation, 2);
            sleepUntil(()->NavigationState.IDLE == navigationState);
            return inventoryAndBankManagementScript.openNearestBank();
        }
    }
    public boolean navigateWithAntiBan(WorldPoint destination, int distance) {
        // get antiban settings
        //Rs2AntibanSettings Rs2AntibanSettings = new Rs2AntibanSettings();
        Microbot.pauseAllScripts = false;
        
        return navigateTo(Rs2Player.getWorldLocation(), 2);
        //Rs2Antiban.actionCooldown();
        //Rs2Antiban.takeMicroBreakByChance();
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
    public boolean navigateToAreaByName(String  AreaName, int radius) {
        //notimplented yet
        assert false;
        WorldPoint currentLocation = Rs2Player.getWorldLocation();
        // logic for getting area location coordiantes
        
        if (currentLocation != null) {
             
            WorldPoint destinationArea = null;
            // First, try to teleport close to the destination
            //TODO implement this
            //TeleportationType nearestTeleport = findNearestTeleport(destination);
            WorldPoint nearestTeleportationLocation = null;
            //
            if (nearestTeleportationLocation != null) {
                return false;
                //HunterMovementUtils.teleportToLocation(nearestTeleport, destination);
            }
            
            // Then, walk to the exact location
            return navigateTo(destinationArea,radius);
        }
        return false;   
    }

    
    public boolean navigateToAreaByName(String areaName) {
        return navigateToAreaByName(areaName, 2);
        
    }
    public boolean canNavigateToObject(GameObject gameObject) {
        return !Rs2Tile.areSurroundingTilesWalkable(gameObject.getWorldLocation(), gameObject.sizeX(), gameObject.sizeY());
    }
      
    

    public boolean useItemOnObject(String itemName, int objectId, String waitWidgetText ) {
        
        TileObject targetObject = Rs2GameObject.findObjectById(objectId);            
        if (Rs2Inventory.hasItem(itemName)) {
            Microbot.log("Using " + itemName + " on " + targetObject.getCanvasLocation());
        }else{
            Microbot.log(itemName + " not in inventory. ," + targetObject!= null ? "": Integer.toString(targetObject.getId()));
            return false;
        }
        List<Rs2Item> primaryItems = Rs2Inventory.items().stream().filter(x -> x.name.equalsIgnoreCase(itemName)).collect(Collectors.toList());
        List<TileObject> secondaryObjects = Arrays.asList(targetObject);
        Rs2Item closestPrimaryItem = null;
        TileObject closestTileObject = null;
        int minSlotDifference = Integer.MAX_VALUE;

        // Compare each primary item with each secondary item to find the closest slots
        for (Rs2Item primaryItem : primaryItems) {
            for (TileObject secondaryTileObject : secondaryObjects) {
                
                
            }
        }
        Rs2Inventory.useItemOnObject(closestPrimaryItem.getId(), closestTileObject.getId());
        if (waitWidgetText != null) {
            sleepUntil(() -> !Rs2Player.isMoving() && Rs2Widget.findWidget(waitWidgetText, null, false) != null );
        }else{
            sleepUntil(() -> !Rs2Player.isMoving());
        }
        return true;
        
    }

 

    private static int getSkillLevel(Skill skill) {
        return Microbot.getClient().getRealSkillLevel(skill);
    }


    @Override
    public void shutdown() {
        super.shutdown();
        Microbot.pauseAllScripts = true;
    }
}