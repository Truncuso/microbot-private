package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;

import java.util.List;

public class StandardTeleport extends AbstractTeleport {
    private WorldPoint destination;

    public StandardTeleport(String name, WorldPoint destination, TeleportType type, int requiredLevel, List<String> requiredItems, List<String> requiredQuests) {
        super(name, type, requiredLevel, requiredItems, requiredQuests);
        this.destination = destination;
    }

    public WorldPoint getDestination() { return destination; }

    @Override
    public double getDistanceTo(WorldPoint destination) {
        return this.destination.distanceTo2D(destination);
    }
}