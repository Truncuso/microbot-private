package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;
@Getter
@Setter

public class WikiMapResult extends ScraperResult{
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
