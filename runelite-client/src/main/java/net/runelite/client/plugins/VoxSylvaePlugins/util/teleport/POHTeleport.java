package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;

import java.util.List;
import java.util.Map;

public class POHTeleport extends Teleport {
    private String furniture;
    private Map<String, List<POHDestination>> teleports;

    public POHTeleport(String name, String furniture, Map<String, List<POHDestination>> teleports, int requiredLevel) {
        super(name, null, TeleportType.POH, requiredLevel, null, null);
        this.furniture = furniture;
        this.teleports = teleports;
    }

    public String getFurniture() { return furniture; }
    public Map<String, List<POHDestination>> getTeleports() { return teleports; }

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