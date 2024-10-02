package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ItemResult extends ScraperResult {
    private List<String> names;
    private List<List<Integer>> ids;
    private List<String> imagePaths;
    private List<Boolean> noteable;
    private List<List<String>> options;
    private List<Boolean> stackable;
    private List<Boolean> tradeable;
    private List<Boolean> equipable;
    private List<String> examine;
    private List<Integer> highAlchValue;
    private List<Integer> gePrice;
    private List<ShopInfo> shops;
    private List<String> locations;
    private List<String> droppedBy;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ItemResult{\n");

        for (int i = 0; i < names.size(); i++) {
            sb.append("  Version ").append(i + 1).append(":\n");
            sb.append("    Name: ").append(names.get(i)).append("\n");
            sb.append("    IDs: ").append(ids.get(i)).append("\n");
            sb.append("    Noteable: ").append(noteable.get(i)).append("\n");
            sb.append("    Options: ").append(options.get(i)).append("\n");
            sb.append("    Stackable: ").append(stackable.get(i)).append("\n");
            sb.append("    Tradeable: ").append(tradeable.get(i)).append("\n");
            sb.append("    Equipable: ").append(equipable.get(i)).append("\n");
            sb.append("    Examine: ").append(examine.get(i)).append("\n");
            sb.append("    High Alch Value: ").append(highAlchValue.get(i)).append("\n");
            sb.append("    GE Price: ").append(gePrice != null && i < gePrice.size() ? gePrice.get(i) : "N/A").append("\n");
        }

        sb.append("  Common Properties:\n");
        sb.append("    Image Paths: ").append(imagePaths).append("\n");
        sb.append("    Shops: ").append(shops).append("\n");
        sb.append("    Locations: ").append(locations).append("\n");
        sb.append("    Dropped By: ").append(droppedBy).append("\n");

        sb.append("}");
        return sb.toString();
    }
}