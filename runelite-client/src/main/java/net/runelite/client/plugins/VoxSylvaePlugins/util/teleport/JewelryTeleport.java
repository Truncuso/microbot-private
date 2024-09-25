package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;
import java.util.List;

public class JewelryTeleport implements Teleport {
    private String name;
    private int charges;
    private List<JewelryDestination> destinations;

    public JewelryTeleport(String name, int charges, List<JewelryDestination> destinations) {
        this.name = name;
        this.charges = charges;
        this.destinations = destinations;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TeleportType getType() {
        return TeleportType.JEWELRY;
    }

    @Override
    public int getRequiredLevel() {
        return 0; // Jewelry doesn't typically have a level requirement
    }

    @Override
    public List<String> getRequiredItems() {
        return List.of(name); // The jewelry item itself is required
    }

    @Override
    public List<String> getRequiredQuests() {
        return null; // Typically, jewelry doesn't have quest requirements
    }

    @Override
    public double getDistanceTo(WorldPoint destination) {
        double minDistance = Double.MAX_VALUE;
        for (JewelryDestination jewelryDest : destinations) {
            double distance = jewelryDest.getCoordinates().distanceTo2D(destination);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    public int getCharges() {
        return charges;
    }

    public List<JewelryDestination> getDestinations() {
        return destinations;
    }

    public static class JewelryDestination {
        private String name;
        private WorldPoint coordinates;
        private boolean membersOnly;

        public JewelryDestination(String name, WorldPoint coordinates, boolean membersOnly) {
            this.name = name;
            this.coordinates = coordinates;
            this.membersOnly = membersOnly;
        }

        public String getName() {
            return name;
        }

        public WorldPoint getCoordinates() {
            return coordinates;
        }

        public boolean isMembersOnly() {
            return membersOnly;
        }
    }
}