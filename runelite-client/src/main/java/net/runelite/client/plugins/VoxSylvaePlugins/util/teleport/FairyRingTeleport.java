package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;
import java.util.List;

public class FairyRingTeleport implements Teleport {
    private String code;
    private String location;
    private WorldPoint coordinates;
    private String notes;

    public FairyRingTeleport(String code, String location, WorldPoint coordinates, String notes) {
        this.code = code;
        this.location = location;
        this.coordinates = coordinates;
        this.notes = notes;
    }

    @Override
    public String getName() {
        return "Fairy Ring " + code;
    }

    @Override
    public TeleportType getType() {
        return TeleportType.FAIRY_RING;
    }

    @Override
    public int getRequiredLevel() {
        return 0; // Fairy rings don't have a level requirement
    }

    @Override
    public List<String> getRequiredItems() {
        return List.of("Dramen staff", "Lunar staff"); // Either staff is required
    }

    @Override
    public List<String> getRequiredQuests() {
        return List.of("Fairy Tale II - Cure a Queen (partial completion)");
    }

    @Override
    public double getDistanceTo(WorldPoint destination) {
        return coordinates.distanceTo2D(destination);
    }

    public String getCode() {
        return code;
    }

    public String getLocation() {
        return location;
    }

    public WorldPoint getCoordinates() {
        return coordinates;
    }

    public String getNotes() {
        return notes;
    }
}