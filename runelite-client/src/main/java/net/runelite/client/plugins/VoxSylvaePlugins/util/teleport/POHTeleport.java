package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;

import java.util.List;
import java.util.Map;

public class POHTeleport extends AbstractTeleport {
    private String furniture;
    private Map<String, List<POHDestination>> teleports;

    public POHTeleport(String name, String furniture, Map<String, List<POHDestination>> teleports, int requiredLevel, List<String> requiredItems, List<String> requiredQuests) {
        super(name, TeleportType.POH, requiredLevel, requiredItems, requiredQuests);
        this.furniture = furniture;
        this.teleports = teleports;
    }

    public String getFurniture() { return furniture; }
    public Map<String, List<POHDestination>> getTeleports() { return teleports; }

    @Override
    public double getDistanceTo(WorldPoint destination) {
        double minDistance = Double.MAX_VALUE;
        for (List<POHDestination> destinations : teleports.values()) {
            for (POHDestination pohDest : destinations) {
                double distance = pohDest.getCoordinates().distanceTo2D(destination);
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
        }
        return minDistance;
    }

    public static class POHDestination {
        private String name;
        private WorldPoint coordinates;

        public POHDestination(String name, WorldPoint coordinates) {
            this.name = name;
            this.coordinates = coordinates;
        }

        public String getName() { return name; }
        public WorldPoint getCoordinates() { return coordinates; }
    }
}