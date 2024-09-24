package net.runelite.client.plugins.VoxSylvaePlugins.util.navigation;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.api.Skill;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Script;


import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;


import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;
import java.util.Random;


import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import static net.runelite.client.plugins.microbot.util.Global.sleep;

public class VoxSylvaeNavigationScript extends Script {
    enum NavigationState {
        IDLE,
        WALKING,
        TELEPORTING,
        BANKING,
        INTERACTING
    }
    private static Map<String, TeleportInfo> availableTeleports = new HashMap<>();
    private static final WorldPoint BANK_LOCATION = new WorldPoint(3183, 3436, 0); // Example: Varrock West Bank
    private NavigationState navigationState = NavigationState.IDLE;
    private WorldPoint currentDesiredLocation;
    public NavigationState getNavigationState() {
        return navigationState;
    }
    public WorldPoint getCurrentDesiredLocation() {
        return currentDesiredLocation;
    }
    public static void initialize() {
        loadAllTeleports();
        checkPlayerTeleports();
    }

    private static void loadAllTeleports() {
        // Add all possible teleports here
        availableTeleports.put("Varrock", new TeleportInfo(Rs2Magic.Spell.VARROCK_TELEPORT, new WorldPoint(3212, 3424, 0), 25, "Law rune", "Air rune", "Fire rune"));
        availableTeleports.put("Lumbridge", new TeleportInfo(Rs2Magic.Spell.LUMBRIDGE_TELEPORT, new WorldPoint(3225, 3219, 0), 31, "Law rune", "Air rune", "Earth rune"));
        availableTeleports.put("Falador", new TeleportInfo(Rs2Magic.Spell.FALADOR_TELEPORT, new WorldPoint(2964, 3379, 0), 37, "Law rune", "Air rune", "Water rune"));
        // Add more teleports as needed
    }
    private boolean hasAllItemsInInventory(List<String> items) {
        for (String item : items) {
            if (!Rs2Inventory.hasItem(item)) {
                return false;
            }
        }
        return true;
    }
    private void checkPlayerTeleports() {
        int magicLevel = getSkillLevel(Skill.MAGIC);
        for (Map.Entry<String, TeleportInfo> entry : availableTeleports.entrySet()) {
            TeleportInfo info = entry.getValue();
            if (magicLevel >= info.requiredLevel && hasAllItemsInInventory(info.requiredItems)) {
                info.isAvailable = true;
            }
        }
    }

    public boolean teleportToLocation(String teleportName, WorldPoint finalDestination) {
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
    }

    public boolean walkTo(WorldPoint destination, int distance) {
        this.navigationState = NavigationState.WALKING;
        this.currentDesiredLocation = destination;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                Microbot.log("Walk todestination.");
                //Rs2Walker.walkTo(NORTH_OF_WEB, 2);
                //return sleepUntil(() -> Rs2Walker.getDistanceBetween(playerLocation, NORTH_OF_WEB) < 5 && !Rs2Player.isMoving(), 300);
                sleepUntil(() -> Rs2Walker.walkTo(destination, distance) && !Rs2Player.isMoving(), 300);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        //sleepUntil(() -> Rs2Walker.walkTo(destination, distance) && !Rs2Player.isMoving(), 300);
        this.navigationState = NavigationState.IDLE;
        return true;
    }
    public boolean walkToAreabyName(String  AreaName, int radius) {
        //notimplented yet
        assert false;
        WorldPoint destinationArea = NULL;
        return walkTo(destinationArea, radius);        
    }

    public static boolean useTransportation(String transportType, int objectId) {
        if (transportType.equals("Fairy Ring")) {
            Rs2GameObject fairyRing = Rs2GameObject.findObjectById(objectId);
            if (fairyRing != null) {
                return Rs2GameObject.interact(fairyRing, "Configure");
            }
        } else {
        
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
                Rs2GameObject transportObject = Rs2GameObject.findObjectById(objectId);
                if (transportObject != null) {
                    return Rs2GameObject.interact(transportObject, transportType);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }, 0, 1000, TimeUnit.MILLISECONDS);  
        }
        return false;
    }

    public static boolean useItemOnObject(String itemName, int objectId) {
        if (Rs2Inventory.hasItem(itemName)) {
            Rs2GameObject targetObject = Rs2GameObject.findObjectById(objectId);
            if (targetObject != null) {
                Rs2Inventory.get(itemName).useOn(targetObject);
                return true;
            }
        }
        return false;
    }

    public boolean performTeleport(String teleportName) {
        navigationState = NavigationState.TELEPORTING;
        TeleportInfo teleport = availableTeleports.get(teleportName);

        if (teleport == null) {
            System.out.println("Unknown teleport: " + teleportName);
            navigationState = NavigationState.IDLE;
            return false;
        }

        List<String> missingItems = new ArrayList<>();
        for (String item : teleport.requiredItems) {
            if (!Rs2Inventory.hasItem(item)) {
                missingItems.add(item);
            }
        }

        if (!missingItems.isEmpty()) {
            navigationState = NavigationState.BANKING;
            // get missing items from bank not implemented yet
            assert false;
            navigationState = NavigationState.IDLE;
            return False
        }
        
        
        navigationState = NavigationState.IDLE;
        return true;
    }

    
    public boolean navigateToArea(String areaName) {
        WorldPoint currentLocation = Rs2Player.getWorldLocation();
        // logic for getting area location coordiantes
        
        if (currentLocation != null) {
             
            WorldPoint destinationArea = NULL;
            // First, try to teleport close to the destination
            //TODO implement this
            //TeleportationType nearestTeleport = findNearestTeleport(destination);
            //WorldPoint nearestTeleportationLocation
            //
            if (nearestTeleportationLocation != null) {
                return false
                //HunterMovementUtils.teleportToLocation(nearestTeleport, destination);
            }
            
            // Then, walk to the exact location
            return walkTo(destinationArea,0);
        }
        return false;
    }
    private int getSkillLevel(Skill skill) {
        return Microbot.getClient().getRealSkillLevel(skill);
    }
    private String findNearestTeleport(WorldPoint destination) {
        // Implement logic to find the nearest teleport to the destination
        // Return the teleport method name or null if no suitable teleport is found
        return null;
    }
    private static class TeleportInfo {
        
        MagicAction spell;
        WorldPoint destination;
        int requiredLevel;
        List<String> requiredItems;
        boolean isAvailable;

        TeleportInfo(MagicAction spell, WorldPoint destination, int requiredLevel, String... requiredItems) {
            this.spell = spell;
            this.destination = destination;
            this.requiredLevel = requiredLevel;
            this.requiredItems = requiredItems;
            this.isAvailable = false;
        }
    }
}