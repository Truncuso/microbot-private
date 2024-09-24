package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import net.runelite.api.coords.WorldPoint;

public class FairyRingTeleport extends Teleport {
    private String code;
    private String notes;

    public FairyRingTeleport(String name, WorldPoint destination, String code, String notes) {
        super(name, destination, TeleportType.FAIRY_RING, 0, null, null);
        this.code = code;
        this.notes = notes;
    }

    public String getCode() { return code; }
    public String getNotes() { return notes; }
}