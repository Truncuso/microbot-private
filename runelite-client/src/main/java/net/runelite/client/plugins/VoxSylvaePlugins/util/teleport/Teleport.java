package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.Quest;
import java.util.List;

public interface Teleport {
    String getName();
    TeleportType getType();
    int getRequiredLevel();
    List<ItemQuantity> getRequiredItems();
    List<String> getRequiredQuestsString();
    List<Quest> getRequiredQuests();
    List<WorldPoint> getDestinations();
    double getDistanceTo(WorldPoint destination);
    boolean isEquippable();
    int getCharges();    
    
}