package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;

import java.util.List;

public class JewelryTeleport extends Teleport {
    private int charges;
    private List<JewelryDestination> destinations;

    public JewelryTeleport(String name, int charges, List<JewelryDestination> destinations) {
        super(name, null, TeleportType.JEWELRY, 0, null, null);
        this.charges = charges;
        this.destinations = destinations;
    }

    public int getCharges() { return charges; }
    public List<JewelryDestination> getDestinations() { return destinations; }

    public static class JewelryDestination {
        private String name;
        private WorldPoint coordinates;
        private boolean membersOnly;

        public JewelryDestination(String name, WorldPoint coordinates, boolean membersOnly) {
            this.name = name;
            this.coordinates = coordinates;
            this.membersOnly = membersOnly;
        }

        public String getName() { return name; }
        public WorldPoint getCoordinates() { return coordinates; }
        public boolean isMembersOnly() { return membersOnly; }
    }
}