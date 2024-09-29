package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.shortestpath.Transport;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors; 

public class ShortestPathResult {
    private final List<WorldPoint> path;
    private final Map<WorldPoint, List<Transport>> transports;
    private final int distance;
    private final WorldPoint start;
    private final WorldPoint end;
    ShortestPathResult(List<WorldPoint> path, Map<WorldPoint, List<Transport>> transports, int distance, WorldPoint start, WorldPoint end) {
        this.path = path;
        this.transports = transports;
        this.distance = distance;
        this.start = start;
        this.end = end;
    }
    public int getDistance() {
        return distance;
    }
    public WorldPoint getStart() {
        return start;
    }
    public WorldPoint getEnd() {
        return end;
    }
    public List<WorldPoint> getPath() {
        return path;
    }

    public Map<WorldPoint, List<Transport>> getTransports() {
        return transports;
    }
}