package net.runelite.client.plugins.truncplugins.skilling.hunter.truncHuntersRumours;

import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.FishingSpot;
import net.runelite.client.plugins.agility.AgilityPlugin;
import net.runelite.client.plugins.agility.Obstacle;
import net.runelite.client.plugins.agility.Obstacles;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.agility.MicroAgilityConfig;

import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.DropOrder;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import com.google.common.collect.ImmutableSet;
import net.runelite.api.*;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.api.NullObjectID.*;
import static net.runelite.api.ObjectID.LADDER_36231;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.validateInteractable;

public class HuntersRumoursScript extends Script {

    public static String version = "0.0.1";
    public static int timeout = 0;
    private HuntersRumoursConfig config;
    private HunterRumoursTaskManager rumoursTaskManager;
    public enum HunterState {
        CHECK_REQUIREMENTS,   // Check if the player meets the requirements for the task
        SELECT_TASK_MASTER,   // Select the task master (configured or default)
        GET_TASK,             // Get a new task from the task master
        NAVIGATE_TO_HUNT,     // Navigate to the hunting area
        PERFORM_HUNTING,      // Perform the hunting task
        CHECK_BREAK,          // Check if a break should be taken
        GET_NEW_TASK,         // After hunting, get a new task
        FINISHED              // Bot is done with the task or has finished
    }
    
    private void init(HuntersRumoursConfig config) {
        
        int hunterLevel = getSkillLevel(Skill.HUNTER);
        HunterTask currentTask = rumoursTaskManager.getRumourTaskForLevel(hunterLevel);

    }
    public boolean run(HuntersRumoursConfig config) {
        this.config = config;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyHunterSetup();
        

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
          try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (startCourse == null) {
                    Microbot.showMessage("Agility course: " + config.agilityCourse().name() + " is not supported.");
                    sleep(10000);
                    return;
                }

                
                final LocalPoint playerLocation = Microbot.getClient().getLocalPlayer().getLocalLocation();
                final WorldPoint playerWorldLocation = Microbot.getClient().getLocalPlayer().getWorldLocation();

                // Eat food.
                Rs2Player.eatAt(config.hitpoints());
                if (random(1, 10) == 2 && config.pauseRandomly()) {
                    sleep(random(config.pauseMinTime(), config.pauseMaxTime()));
                }

                if (Rs2Player.isMoving()) return;
                if (Rs2Player.isAnimating()) return;

                if (currentObstacle >= getCurrentCourse(config).size()) {
                    currentObstacle = 0;
                }

                if (config.agilityCourse() == PRIFDDINAS_AGILITY_COURSE) {
                    TileObject portal = Rs2GameObject.findObject(PORTAL_OBSTACLE_IDS.stream().collect(Collectors.toList()));

                    if (portal != null && Microbot.getClientThread().runOnClientThread(() -> portal.getClickbox()) != null) {
                        if (Rs2GameObject.interact(portal, "travel")) {
                            sleep(2000, 3000);
                            return;
                        }
                    }
                }

                if (Microbot.getClient().getTopLevelWorldView().getPlane() == 0 && playerWorldLocation.distanceTo(startCourse) > 6 && config.agilityCourse() != GNOME_STRONGHOLD_AGILITY_COURSE) {
                    currentObstacle = 0;
                    LocalPoint startCourseLocal = LocalPoint.fromWorld(Microbot.getClient().getTopLevelWorldView(), startCourse);
                    if (startCourseLocal == null || playerLocation.distanceTo(startCourseLocal) >= MAX_DISTANCE) {
                        if (config.alchemy()) {
                            Rs2Magic.alch(config.item(), 50, 100);
                        }
                        if (Rs2Player.getWorldLocation().distanceTo(startCourse) < 100) {//extra check for prif course
                            Rs2Walker.walkTo(startCourse, 8);
                            return;
                        }
                    }
                }

            
                
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            


        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    public void onGameTick() {
      
    }
    private int getSkillLevel(Skill skill) {
        return Microbot.getClient().getRealSkillLevel(skill);
    }

    private void performHunterTask(HunterTask task) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location
    }

    private void handleState() {
        switch (currentState) {
            case CHECK_REQUIREMENTS:
                if (checkHunterRequirements()) {
                    currentState = HunterState.SELECT_TASK_MASTER;
                }
                break;
                
            case SELECT_TASK_MASTER:
                taskMaster = selectTaskMaster();
                currentState = HunterState.GET_TASK;
                break;
                
            case GET_TASK:
                if (hasCurrentTask()) {
                    currentTask = checkAssignedTask();
                    currentState = HunterState.NAVIGATE_TO_HUNT;
                } else {
                    getNewHunterTask(taskMaster);
                    currentTask = checkAssignedTask();
                    currentState = HunterState.NAVIGATE_TO_HUNT;
                }
                break;

            case NAVIGATE_TO_HUNT:
                if (navigateToHuntingArea(currentTask)) {
                    currentState = HunterState.PERFORM_HUNTING;
                }
                break;

            case PERFORM_HUNTING:
                if (performHunting(currentTask)) {
                    currentState = HunterState.CHECK_BREAK;
                }
                break;

            case CHECK_BREAK:
                if (shouldTakeBreak()) {
                    takeBreak();
                }
                currentState = HunterState.GET_NEW_TASK;
                break;

            case GET_NEW_TASK:
                rinseAndRepeat();
                break;

            case FINISHED:
                stop();
                break;
        }
    }


    
    /*In the performHunterTask method, implement the bot's logic for:

    Traveling to the hunting area.
    Setting traps (based on the trap type).
    Catching creatures.
    Periodically checking the traps, resetting them as needed.
    Handling antiban actions (camera movements, mouse movements) between actions to simulate human-like behavior.*/
    private void performHunterTask(HunterTask task) {
        // Logic to set traps, catch creatures, and reset traps as necessary
    
        if (atCorrectLocation(task.getLocation())) {
            if (trapsNeedResetting()) {
                resetTraps(task.getTrapType());
            } else {
                waitForCatches();
            }
        } else {
            travelToLocation(task.getLocation());
        }
    }
    
    private void walkToHunterArea(WorldPoint destination) {
        Microbot.getWalker().walkTo(destination);
        if (Microbot.getClient().getTopLevelWorldView().getPlane() == 0 && playerWorldLocation.distanceTo(destination) > 6 && config.agilityCourse() != GNOME_STRONGHOLD_AGILITY_COURSE) {
            currentObstacle = 0;
            LocalPoint startCourseLocal = LocalPoint.fromWorld(Microbot.getClient().getTopLevelWorldView(), startCourse);
            if (startCourseLocal == null || playerLocation.distanceTo(startCourseLocal) >= MAX_DISTANCE) {
                if (config.alchemy()) {
                    Rs2Magic.alch(config.item(), 50, 100);
                }
                if (Rs2Player.getWorldLocation().distanceTo(startCourse) < 100) {//extra check for prif course
                    Rs2Walker.walkTo(startCourse, 8);
                    return;
                }
            }
        }
    }
    
    private void dropInventoryItems(HuntersRumoursConfig config) {
        DropOrder dropOrder = config.dropOrder() == DropOrder.RANDOM ? DropOrder.random() : config.dropOrder();
        Rs2Inventory.dropAll(x -> x.name.toLowerCase().contains("leaping"), dropOrder);
    }

    public void shutdown() {
        Rs2Antiban.resetAntibanSettings();
        super.shutdown();
    }

    private boolean hasCurrentTask() {
        // Logic to determine if the player already has a task (e.g., by checking inventory or a task scroll)
        return Microbot.getInventory().contains("Task Scroll");
    }
    
    private void getNewHunterTask(TaskMaster master) {
        // Walk to the task master, only one step at a time
        Microbot.getWalker().walkTo(master.getLocation());
    
        // Interact with the task master to get a new task (performed over multiple ticks)
        interactWithTaskMaster(master);
    }

}