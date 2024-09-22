package net.runelite.client.plugins.truncplugins.skilling.hunter.truncHuntersRumours;
import java.util.List;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
public class TaskMaster {
    private String name;
    private String tier;
    private int requiredLevel;
    private WorldPoint worldPoint;
    private List<String> creatures;
    private String questRequirement;

    public TaskMaster(String name, String tier, int requiredLevel, WorldPoint worldPoint, List<String> creatures, String questRequirement) {
        this.name = name;
        this.tier = tier;
        this.requiredLevel = requiredLevel;
        this.worldPoint = worldPoint;
        this.creatures = creatures;
        this.questRequirement = questRequirement;
    }

    // Getters
    public String getName() { return name; }
    public String getTier() { return tier; }
    public int getRequiredLevel() { return requiredLevel; }
    public WorldPoint getWorldPoint() { return worldPoint; }
    public List<String> getCreatures() { return creatures; }
    public String getQuestRequirement() { return questRequirement; }
}