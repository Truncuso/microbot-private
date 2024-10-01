package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

@Getter
@Setter
public abstract class ScraperResult {
    // Inner class to represent item quantities
    public static class ItemQuantity {
        private final String itemName;
        private final int quantity;

        public ItemQuantity(String itemName, int quantity) {
            this.itemName = itemName;
            this.quantity = quantity;
        }

        // Getters
        public String getItemName() { return itemName; }
        public int getQuantity() { return quantity; }
    }
    private List<String> names;
    private List<Integer> ids;
    private List<String> imagePaths;
    private Map<String, String> additionalInfo;
    @Getter
    @Setter
    public static class LocationResult extends ScraperResult {
        private String name;
        private boolean members;
        private String type;
        private String location;
        private MapResult map;
        private String image;
        private String category;

        // Getters and setters
    }
    @Getter
    @Setter
    public static class TeleportationSpellResult extends ScraperResult {
        private String name;
        private String destination;
        private List<ItemQuantity> cost;
        private String spellbook;
        private String type;
        private int level;
        private String image;

        // Getters and setters
    }
    @Getter
    @Setter
    public static class TeleportationItemResult extends ScraperResult {
        private String name;
        private List<WorldPoint> destinations;
        private String type;
        private ItemData itemData;

        // Getters and setters
    }
    @Getter
    @Setter
    public static class FairyRingResult extends ScraperResult {
        private String code;
        private String destination;
        private String name;
        private String location;

        // Getters and setters
    }
    @Getter
    @Setter
    public static class ItemData {
        private boolean equipable;
        private boolean tradeable;
        private boolean stackable;
        private List<String> options;
        private boolean noteable;
        private List<String> names;
        private List<Integer> ids;
        private List<String> imagePaths;

        // Getters and setters
    }

    @Getter
    @Setter
    public static class MapResult extends ScraperResult {
        private String name;
        private int x;
        private int y;
        private int plane;
        private int mapID;
        private String mtype;
        private int r;
        private int squareX;
        private int squareY;
        private String ptype;
        private List<String> imagePaths;
    
        // Getters and setters
    }
    @Getter
    @Setter
    public static class NPCResult extends ScraperResult {
        private String name;
        private List<String> names; // For monsters with multiple versions
        private int id;
        private List<Integer> ids; // For monsters with multiple versions
        private List<String> imagePaths;
        private String examine;
        private int combatLevel;
        private List<String> attackTypes;
        private Map<String, Integer> stats;
        private List<ItemResult> drops;
        private String location;
        private boolean aggressive;
        private boolean isMonster;

        // Additional fields that might be useful
        private int hitpoints;
        private int maxHit;
        private String weakness;
        private List<String> attributes; // e.g., "undead", "demon", etc.

        @Override
        public String toString() {
            return "NPCResult{" +
                    "name='" + name + '\'' +
                    ", names=" + names +
                    ", id=" + id +
                    ", ids=" + ids +
                    ", imagePaths=" + imagePaths +
                    ", examine='" + examine + '\'' +
                    ", combatLevel=" + combatLevel +
                    ", attackTypes=" + attackTypes +
                    ", stats=" + stats +
                    ", drops=" + drops +
                    ", location='" + location + '\'' +
                    ", aggressive=" + aggressive +
                    ", isMonster=" + isMonster +
                    ", hitpoints=" + hitpoints +
                    ", maxHit=" + maxHit +
                    ", weakness='" + weakness + '\'' +
                    ", attributes=" + attributes +
                    '}';
        }
    }

    @Getter
    @Setter
    public static class ItemResult extends ScraperResult {
        private String name;
        private List<Integer> ids;
        private List<String> imagePaths;
        private boolean noteable;
        private List<String> options;
        private boolean stackable;
        private boolean tradeable;
        private boolean equipable;
        private String examine;
        private List<Integer> quantity; // For drops
        private double rarity; // For drops
        private boolean lootStatus; // For drops

        @Override
        public String toString() {
            return "ItemResult{" +
                    "name='" + name + '\'' +
                    ", ids=" + ids +
                    ", imagePaths=" + imagePaths +
                    ", noteable=" + noteable +
                    ", options=" + options +
                    ", stackable=" + stackable +
                    ", tradeable=" + tradeable +
                    ", equipable=" + equipable +
                    ", examine='" + examine + '\'' +
                    ", quantity=" + quantity +
                    ", rarity=" + rarity +
                    ", lootStatus=" + lootStatus +
                    '}';
        }
    }

}