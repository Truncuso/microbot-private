package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;

import java.util.List;

public class Teleport {
    private String name;
    private WorldPoint destination;
    private TeleportType type;
    private int requiredLevel;
    private List<String> requiredItems;
    private List<String> requiredQuests;

    public Teleport(String name, WorldPoint destination, TeleportType type, int requiredLevel, List<String> requiredItems, List<String> requiredQuests) {
        this.name = name;
        this.destination = destination;
        this.type = type;
        this.requiredLevel = requiredLevel;
        this.requiredItems = requiredItems;
        this.requiredQuests = requiredQuests;
    }

    // Getters
    public String getName() { return name; }
    public WorldPoint getDestination() { return destination; }
    public TeleportType getType() { return type; }
    public int getRequiredLevel() { return requiredLevel; }
    public List<String> getRequiredItems() { return requiredItems; }
    public List<String> getRequiredQuests() { return requiredQuests; }
}