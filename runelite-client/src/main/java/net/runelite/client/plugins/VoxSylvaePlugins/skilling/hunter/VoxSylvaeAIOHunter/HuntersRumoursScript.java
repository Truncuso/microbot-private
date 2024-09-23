package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;

import java.util.concurrent.TimeUnit;

import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;

public class HuntersRumoursScript extends Script {

    public static String version = "0.0.1";
    public static int timeout = 0;
    
    HunterCreature currentHuntingCreature = null;
    private AIOHunterConfig config;
    private AIOHunterTaskManager rumoursTaskManager;


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
        NAVIGATE_TO_HUNT,     // Navigate to the hunting area
        PERFORM_HUNTING,      // Perform the hunting task
        CHECK_BREAK,          // Check if a break should be taken
        GET_NEW_TASK,         // After hunting, get a new task
        FINISHED              // Bot is done with the task or has finished
    }

    private HunterState currentState = HunterState.INITIALIZE;
    private void initialize(AIOHunterConfig config) {
        
        int hunterLevel = getSkillLevel(Skill.HUNTER);
        /*HunterTask currentTask = rumoursTaskManager.getRumourTaskForLevel(hunterLevel);*/
        //rumoursTaskManager = new HunterRumoursTaskManager();
        //classicHuntingManager = new ClassicHuntingManager();
        this.config = config;
        currentState = HunterState.CHECK_REQUIREMENTS;

    }
    public boolean run(AIOHunterConfig config) {        
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyHunterSetup();
                        
        initialize(config);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
          try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                /*if (startCourse == null) {
                    Microbot.showMessage("Agility course: " + config.agilityCourse().name() + " is not supported.");
                    sleep(10000);
                    return;
                }*/

                
                final LocalPoint playerLocation = Microbot.getClient().getLocalPlayer().getLocalLocation();
                final WorldPoint playerWorldLocation = Microbot.getClient().getLocalPlayer().getWorldLocation();
                handleState();
            
                
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            


        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    public void onGameTick() {
      
    }
    
    public void shutdown() {
        Rs2Antiban.resetAntibanSettings();
        super.shutdown();
    }
    public void onChatMessage(ChatMessage event) {
    
    }
    public void onProfileChanged(ProfileChanged event) {
    }
    private int getSkillLevel(Skill skill) {
        return Microbot.getClient().getRealSkillLevel(skill);
    }

  

    private void handleState() {
          switch (currentState) {
            case INITIALIZE:
                initializeHunting();
                break;
            case SELECT_TASK:
                selectNewHunterTask();
                currentState = HunterState.GET_TASK;
                break;
            case GET_TASK:
                getNewHunterTask();
                currentState = HunterState.CHECK_REQUIREMENTS;
                break;
            
            case CHECK_REQUIREMENTS:
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
                
                performHunting(this.currentHuntingCreature);
                break;
            case CHECK_BREAK:
                break;
            case FINISHED:
                //stop();
                break;
        }
    }

    private void initializeHunting() {
        //if (config.huntingMode() == HuntersRumoursConfig.HuntingMode.HUNTER_RUMOURS) {
        //    rumoursTaskManager.initialize();
        //} else {
        //    classicHuntingManager.initialize(config.classicHuntingCreature());
        //}
        
    }
    private void performHunting(HunterCreature creature) {
        // Add logic to set traps, catch animals, and complete the task
        // This would include using the trap type in the specified location
    }
    private boolean checkHunterRequirements() {
        int hunterLevel = getSkillLevel(Skill.HUNTER);
       
    }
    private boolean navigateToHuntingArea() {
        if (config.huntingMode() == HuntersRumoursConfig.HuntingMode.HUNTER_RUMOURS) {
            return rumoursTaskManager.navigateToHuntingArea();
        } else {
            return classicHuntingManager.navigateToHuntingArea();
        }
    }
    private void getNewHunterTask(HunterMaster master) {
        // Walk to the task master, only one step at a time
        //Microbot.getWalker().walkTo(master.getLocation());
    
        // Interact with the task master to get a new task (performed over multiple ticks)
        //interactWithTaskMaster(master);
    }


}