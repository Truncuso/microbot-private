package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;
import java.util.Arrays;
import java.util.List;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;

import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;


public class HunterCreatureTarget {
    private String name;
    private int requiredLevel;
    private String method;
    private List<Integer> lootIds;
    
    private List<CreatureLocation> locations;
    private Goal goal;
    private enum GoalType{
        CATCHES, ITEMS
    }
    private class Goal{
        private String name;
        private int id;
        private int goalAmount;
        private int currentCatchCount;
        private boolean completed;
        private GoalType type;
        public Goal(String name, int amount, GoalType type){
            this.name = name;
            this.goalAmount = goalAmount;
            this.currentCatchCount = 0;
            this.completed = false;
            this.type = type;
            //int unprocessedItemPrice = Microbot.getItemManager().search(config.ITEM().getName()).get(0).getPrice();
            //int processedItemPrice = Microbot.getItemManager().search(config.ITEM().getFinished()).get(0).getPrice();
            //profitPerPlank = processedItemPrice - unprocessedItemPrice;
        }
        public void incrementCatch(){
            this.currentCatchCount++;
            if (type == GoalType.CATCHES && this.currentCatchCount >= this.goalAmount){
                this.completed = true;
            }
            
        }
        public boolean isCompleted(){
            return this.completed;
        }
        public int getGoalAmount(){
            return this.goalAmount;
        }
        public String getGoalName(){
            return this.name;
        }
        public String getGoalTypeName(){
            return this.type.name();
        }
        
    }
    public HunterCreatureTarget(String name, int requiredLevel, String method, List<CreatureLocation> locations) {
        this.name = name;
        this.requiredLevel = requiredLevel;
        this.method = method;
        this.locations = locations;

    }

    // Getters
    public String getName() { return name; }
    public int getRequiredLevel() { return requiredLevel; }
    public String getMethod() { return method; }
    public List<CreatureLocation> getLocations() { return locations; }
    public List<Integer> getLootIds() { return lootIds; }
    public void setLootIds(List<Integer> lootIds) { this.lootIds = lootIds; }
    public int getGoalAmount() { return this.goal.getGoalAmount(); }
    public boolean isCompleted() { return this.goal.isCompleted(); }
    public void incrementCatch() { this.goal.incrementCatch(); }
    public String getGoalName() { return this.goal.getGoalName(); }
    public String getGoalTypeName() { return this.goal.getGoalTypeName(); }
    public List<String> getHuntingAreaNames(){
        
        //HunterAreaUtils hunterAreaUtils = new HunterAreaUtils();
        return HunterAreaUtils.getAreaNamesForCreature(this.name);

    }
    public List<WorldPoint> getHuntingAreaLocations(){
        
        //HunterAreaUtils hunterAreaUtils = new HunterAreaUtils();
        return HunterAreaUtils.getLocationsForCreature(this.name);

    }
}