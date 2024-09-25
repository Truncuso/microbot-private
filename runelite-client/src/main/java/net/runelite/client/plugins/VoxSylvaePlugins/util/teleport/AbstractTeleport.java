package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;

import java.util.List;

public abstract class AbstractTeleport implements Teleport {
    protected String name;
    protected TeleportType type;
    protected int requiredLevel;
    protected List<String> requiredItems;
    protected List<String> requiredQuests;

    public AbstractTeleport(String name, TeleportType type, int requiredLevel, List<String> requiredItems, List<String> requiredQuests) {
        this.name = name;
        this.type = type;
        this.requiredLevel = requiredLevel;
        this.requiredItems = requiredItems;
        this.requiredQuests = requiredQuests;
    }

    @Override
    public String getName() { return name; }

    @Override
    public TeleportType getType() { return type; }

    @Override
    public int getRequiredLevel() { return requiredLevel; }

    @Override
    public List<String> getRequiredItems() { return requiredItems; }

    @Override
    public List<String> getRequiredQuests() { return requiredQuests; }

    @Override
    public abstract double getDistanceTo(WorldPoint destination);
}