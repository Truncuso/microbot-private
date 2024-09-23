package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
public class CreatureLocation {
    private String locationName;
    private WorldPoint worldPoint;

    public CreatureLocation(String locationName, WorldPoint worldPoint) {
        this.locationName = locationName;
        this.worldPoint = worldPoint;
    }

    // Getters
    public String getLocationName() { return locationName; }
    public WorldPoint getWorldPoint() { return worldPoint; }
}