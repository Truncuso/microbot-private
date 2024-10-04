package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ItemSources {
    private List<DropSource> dropSources;
    private List<SpawnLocation> spawnLocations;
    private List<ShopSource> shopSources;

    public boolean hasItemSources() {
        return dropSources != null || spawnLocations != null || shopSources != null;
    }

    public boolean hasDropSources() {
        return dropSources != null;
    }

    public boolean hasSpawnLocations() {
        return spawnLocations != null;
    }

    public boolean hasShopLocations() {
        return shopSources != null;
    }   

    @Override
    public String toString() {
        return "ItemSources{" +
            "dropSources=" + dropSources +
            ", spawnLocations=" + spawnLocations +
            ", shopLocations=" + shopSources +
            '}';
    }
}

