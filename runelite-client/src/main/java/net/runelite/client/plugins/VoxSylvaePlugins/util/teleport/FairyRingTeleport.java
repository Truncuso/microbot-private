package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FairyRingTeleport extends AbstractTeleport {
    private String code;
    private String location;
    private String notes;
    private List<String> pointsOfInterest;

    public FairyRingTeleport(String name, int requiredLevel, List<ItemQuantity> requiredItems, 
                             List<String> requiredQuests, List<WorldPoint> destinations, 
                             String code, String location, String notes, List<String> pointsOfInterest) {
        super(name, TeleportType.FAIRY_RING, requiredLevel, requiredItems, requiredQuests, destinations, false, -1);
        this.code = code;
        this.location = location;
        this.notes = notes;
        this.pointsOfInterest = pointsOfInterest;
    }

    public String getCode() {
        return code;
    }

    public String getLocation() {
        return location;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public boolean isEquippable() {
        return false;  // Fairy rings are not equippable items
    }

    @Override
    public int getCharges() {
        return -1;  // Fairy rings have infinite charges
    }
    public List<String> getPointsOfInterest() {
        return pointsOfInterest;
    }
    public WorldPoint getDestination() {
        assert getDestinations().size() == 1;
        return getDestinations().get(0);
    }
    
}