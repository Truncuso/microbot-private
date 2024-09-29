
package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;
import java.util.List;

public class TeleportItem extends AbstractTeleport {
    
    private final int itemId;
    private final List<String> locations;    // List of locations for each teleport destination
    private final boolean membersOnly;       // If the item is members-only
    private final boolean rechargeable;      // If the item is rechargeable
    private final String notes;              // Any additional notes or information

    public TeleportItem(String name, TeleportType category, int itemId, int requiredLevel, List<ItemQuantity> requiredItems, 
                        List<String> requiredQuests, List<WorldPoint> destinations, boolean isEquippable, int charges,
                        List<String> locations, boolean membersOnly, boolean rechargeable, String notes) {
        super(name, category, requiredLevel, requiredItems, requiredQuests, destinations, isEquippable, charges);
        this.itemId = itemId;
        this.locations = locations;
        this.membersOnly = membersOnly;
        this.rechargeable = rechargeable;
        this.notes = notes;
    }

    // Getter for itemId
    public int getItemId() {
        return itemId;
    }

    // Getter for locations
    public List<String> getLocations() {
        return locations;
    }

    // Getter for membersOnly
    public boolean isMembersOnly() {
        return membersOnly;
    }

    // Getter for rechargeable
    public boolean isRechargeable() {
        return rechargeable;
    }

    // Getter for notes
    public String getNotes() {
        return notes;
    }
}