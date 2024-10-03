package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;
import java.util.stream.Collectors;
@Getter
@Setter
public class WikiLocationResult extends ScraperResult{
    private String name;
    private boolean members;
    private String type;
    private String location;
    private WikiMapResult map;
    private String image;
    private String category;

// Getters and setters
}