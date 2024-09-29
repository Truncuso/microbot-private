package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.QuestState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.api.Quest;
import java.util.ArrayList;
import java.util.List;
public abstract class AbstractTeleport implements Teleport {
    protected String name;
    protected TeleportType type;
    protected int requiredLevel;
    protected List<ItemQuantity> requiredItems;
    protected List<String> requiredQuests;
    protected List<WorldPoint> destinations;
    protected boolean isEquippable;
    protected int charges;

    public AbstractTeleport(String name, TeleportType type, int requiredLevel, List<ItemQuantity> requiredItems, List<String> requiredQuests, List<WorldPoint> destinations, boolean isEquippable, int charges) {
        this.name = name;
        this.type = type;
        this.requiredLevel = requiredLevel;
        this.requiredItems = requiredItems;
        this.requiredQuests = requiredQuests;
        this.destinations = destinations;
        this.isEquippable = isEquippable;
        this.charges = charges;
    }

    // Implement getters for all fields
    


    @Override
    public boolean isEquippable() {
        return isEquippable;
    }

    @Override
    public int getCharges() {
        return charges;
    }

    @Override
    public String getName() { return name; }

    @Override
    public TeleportType getType() { return type; }


    @Override
    public int getRequiredLevel() { return requiredLevel; }

    

    @Override
    public List<Quest> getRequiredQuests() { 
        List<Quest> quests = new ArrayList<>();
        for (String questName : requiredQuests) {
            
            for (Quest quest : Quest.values()) {
                if (quest.getName().equals(questName)) {
                    quests.add(quest);
                    break;
                }
            }
            
            //if (!Rs2Player.isQuestCompleted(quest)) {
            //    return false;
            //}
        }
        return quests; 
    }
    @Override
    public List<String> getRequiredQuestsString() { 
            
            
            return requiredQuests; }
        
    @Override
    public List<WorldPoint> getDestinations() { return destinations; }

    @Override
    public double getDistanceTo(WorldPoint destination) {
        return destinations.stream()
                .mapToDouble(d -> d.distanceTo2D(destination))
                .min()
                .orElse(Double.MAX_VALUE);
    }
    @Override
    public List<ItemQuantity> getRequiredItems() {
        return requiredItems;
    }
}