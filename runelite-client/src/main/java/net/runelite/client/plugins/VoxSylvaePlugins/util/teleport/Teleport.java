package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;

import java.util.List;

public interface Teleport {
    String getName();
    TeleportType getType();
    int getRequiredLevel();
    List<String> getRequiredItems();
    List<String> getRequiredQuests();
    double getDistanceTo(WorldPoint destination);
}
