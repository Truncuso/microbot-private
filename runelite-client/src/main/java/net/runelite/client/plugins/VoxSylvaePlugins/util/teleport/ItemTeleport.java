package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;

public class ItemTeleport extends Teleport {
    private int itemId;
    private boolean consumable;

    public ItemTeleport(String name, WorldPoint destination, int itemId, boolean consumable) {
        super(name, destination, TeleportType.ITEM, 0, null, null);
        this.itemId = itemId;
        this.consumable = consumable;
    }

    public int getItemId() { return itemId; }
    public boolean isConsumable() { return consumable; }
}
