package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;
import java.util.List;

public class SpellbookTeleport extends AbstractTeleport {
    private final String location;
    private final boolean membersOnly;
    public SpellbookTeleport(String name, TeleportType type, int requiredLevel, List<ItemQuantity> requiredItems, 
                             List<String> requiredQuests, List<WorldPoint> destinations, 
                             String location, boolean membersOnly) {
        super(
            name,
            type,
            requiredLevel,
            requiredItems,
            requiredQuests,
            destinations,
            false,  // isEquippable
            -1      // charges (spellbook teleports don't have charges)
        );
        this.location = location;
        this.membersOnly = membersOnly;
    }

    @Override
    public boolean isEquippable() {
        return false;  // Spellbook teleports are not equippable items
    }

    @Override
    public int getCharges() {
        return -1;  // Spellbook teleports don't have charges
    }

    // You might want to add a method to get the rune cost of the spell
    public List<ItemQuantity> getRuneCost() {
        return getRequiredItems();
    }
    public WorldPoint getDestination() {
        assert getDestinations().size() == 1;
        return getDestinations().get(0);
    }
    // Getter for location
    public String getLocation() {
        return location;
    }

    // Getter for membersOnly flag
    public boolean isMembersOnly() {
        return membersOnly;
    }
}