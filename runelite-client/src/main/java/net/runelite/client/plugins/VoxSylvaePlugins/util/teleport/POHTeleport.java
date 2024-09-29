package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class POHTeleport extends AbstractTeleport {
    private String furniture;
    private Map<String, List<POHDestination>> teleports;

    public POHTeleport(String name, String furniture, Map<String, List<POHDestination>> teleports, int requiredLevel, List<ItemQuantity> requiredItems, List<String> requiredQuests) {
        super(
            name,
            TeleportType.POH,
            requiredLevel,
            requiredItems,
            requiredQuests,
            teleports.values().stream()
                .flatMap(List::stream)
                .map(POHDestination::getCoordinates)
                .collect(Collectors.toList()),
            false,  // isEquippable
            -1      // charges (generally infinite for POH teleports)
        );
        this.furniture = furniture;
        this.teleports = teleports;
    }

    public String getFurniture() {
        return furniture;
    }

    public Map<String, List<POHDestination>> getTeleports() {
        return teleports;
    }

    @Override
    public boolean isEquippable() {
        return false;  // POH teleports are not equippable items
    }

    @Override
    public int getCharges() {
        return -1;  // POH teleports generally have infinite charges
    }

    public static class POHDestination {
        private String name;
        private WorldPoint coordinates;

        public POHDestination(String name, WorldPoint coordinates) {
            this.name = name;
            this.coordinates = coordinates;
        }

        public String getName() {
            return name;
        }

        public WorldPoint getCoordinates() {
            return coordinates;
        }
    }
}