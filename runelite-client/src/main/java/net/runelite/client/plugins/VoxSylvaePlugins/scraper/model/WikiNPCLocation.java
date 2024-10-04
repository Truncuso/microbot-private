package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class WikiNPCLocation {
    private String name;
    private String location;
    private List<Integer> levels;
    private boolean members;
    private int mapID;
    private int plane;
    private List<Coordinate> coordinates;
    private String mtype;

    @Getter
    @Setter
    @ToString
    public static class Coordinate {
        private int x;
        private int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}