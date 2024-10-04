package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public class SpawnLocation {
    private String location;
    private String sublocation;
    private List<Coordinate> coordinates;
    private int plane;
    private boolean members;
    private int mapID;

    public SpawnLocation() {
        this.coordinates = new ArrayList<>();
        this.members = false;
        this.plane = -1;
        this.mapID = -1;
        this.sublocation = null;
        this.location = null;
    }

    @Getter
    @Setter
    public static class Coordinate {
        private int x;
        private int y;

        public Coordinate() {
            this(-1, -1);
        }
        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}