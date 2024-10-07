package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.WikiItemResult;

import java.util.stream.Collectors;

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
        private WikiItemResult itemData;

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
   

   
   
}