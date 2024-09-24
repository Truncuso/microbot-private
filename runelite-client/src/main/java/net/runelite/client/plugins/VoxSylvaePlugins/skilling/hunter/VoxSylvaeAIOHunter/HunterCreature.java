package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;
import java.util.List;




public class HunterCreature {
    private String name;
    private int requiredLevel;
    private String method;

    private List<CreatureLocation> locations;

    public HunterCreature(String name, int requiredLevel, String method, List<CreatureLocation> locations) {
        this.name = name;
        this.requiredLevel = requiredLevel;
        this.method = method;
        this.locations = locations;
    }

    // Getters
    public String getName() { return name; }
    public int getRequiredLevel() { return requiredLevel; }
    public String getMethod() { return method; }
    public List<CreatureLocation> getLocations() { return locations; }
}